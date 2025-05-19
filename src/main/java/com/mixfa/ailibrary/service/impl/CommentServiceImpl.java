package com.mixfa.ailibrary.service.impl;

import com.mixfa.ailibrary.misc.ExceptionType;
import com.mixfa.ailibrary.misc.PerUserRateLimiter;
import com.mixfa.ailibrary.misc.Utils;
import com.mixfa.ailibrary.misc.cache.CacheMaintainer;
import com.mixfa.ailibrary.model.Book;
import com.mixfa.ailibrary.model.Comment;
import com.mixfa.ailibrary.model.search.SearchOption;
import com.mixfa.ailibrary.model.user.Account;
import com.mixfa.ailibrary.model.user.HasOwner;
import com.mixfa.ailibrary.service.AiBookDescriptionService;
import com.mixfa.ailibrary.service.BookService;
import com.mixfa.ailibrary.service.CommentService;
import com.mixfa.ailibrary.service.SearchEngine;
import com.mixfa.ailibrary.service.repo.BookRepo;
import com.mixfa.ailibrary.service.repo.CommentsRepo;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import static com.mixfa.ailibrary.misc.Utils.fmt;

@Service
public class CommentServiceImpl implements CommentService {
    private final CommentsRepo commentsRepo;
    private final SearchEngine.ForComments commentSearchEngine;
    private final BookRepo bookRepo;
    private final MongoTemplate mongoTemplate;
    private final BookService bookService;
    private final ChatModel chatModel;
    private final AiBookDescriptionService aiBookDescriptionService;
    private final PerUserRateLimiter rateLimiter;

    private final AiCommentChecker aiCommentChecker = new AiCommentChecker();

    public CommentServiceImpl(CacheMaintainer cacheMaintainer,
                              CommentsRepo commentsRepo,
                              SearchEngine.ForComments commentSearchEngine,
                              BookRepo bookRepo,
                              MongoTemplate mongoTemplate,
                              BookService bookService,
                              ChatModel chatModel,
                              AiBookDescriptionService aiBookDescriptionService) {
        this.commentsRepo = commentsRepo;
        this.commentSearchEngine = commentSearchEngine;
        this.bookRepo = bookRepo;
        this.mongoTemplate = mongoTemplate;
        this.bookService = bookService;
        this.chatModel = chatModel;
        this.aiBookDescriptionService = aiBookDescriptionService;


        this.rateLimiter = new PerUserRateLimiter(
                RateLimiterConfig.custom()
                        .limitForPeriod(1)
                        .timeoutDuration(Duration.ofMillis(250))
                        .limitRefreshPeriod(Duration.ofSeconds(3))
                        .build(),
                cacheMaintainer
        );

    }

    @Override
    public Comment addComment(Comment.AddRequest request) {
        if (!Utils.inBound(0.0, 5.0, request.rate())) throw ExceptionType.INVALID_BOOK_RATE.make(request.rate());

        if (!rateLimiter.acquirePermission())
            throw ExceptionType.rateLimitExceeded();

        var remoteUser = Account.getAuthenticatedAccount();

        if (commentsRepo.existsByBookIdAndOwner(request.bookId(), remoteUser))
            throw ExceptionType.bookAlreadyRated(request.bookId(), remoteUser.getUsername());

        if (!bookRepo.existsById(request.bookId()))
            throw ExceptionType.bookNotFound(request.bookId());

        var book = bookService.findBookOrThrow(request.bookId());

        var validationPassed = aiCommentChecker.checkComment(request.text(), book);
        if (!validationPassed)
            throw ExceptionType.invalidComment();

        var comment = new Comment(book, request.text(), request.rate(), remoteUser);

        return commentsRepo.save(comment);
    }

    @Override
    public void removeComment(Object commentId) {
        var q = Query.query(HasOwner.ownerCriteria().and(Comment.Fields.id).is(Utils.idToObj(commentId)));
        mongoTemplate.remove(q, Comment.class);
    }

    @Override
    public double getBookRate(Object bookId) {
        record AvgRate(double avgRate) {
            static final AvgRate ZERO_RATE = new AvgRate(0.0);
        }

        var q = Criteria.where(fmt("{0}.$id", Comment.Fields.book)).is(Utils.idToObj(bookId));

        var matchOp = Aggregation.match(q);
        var avgRateOp = Aggregation.group().avg(Comment.Fields.rate).as("avgRate");
        var projectOp = Aggregation.project("avgRate");

        var aggrRes = mongoTemplate.aggregate(Aggregation.newAggregation(matchOp, avgRateOp, projectOp), Comment.class, AvgRate.class);

        return Objects.requireNonNullElse(aggrRes.getUniqueMappedResult(), AvgRate.ZERO_RATE).avgRate;
    }

    @Override
    public Page<Comment> listComments(Object bookId, Pageable pageable) {
        return commentSearchEngine.find(
                SearchOption.Comments.byBook(bookId),
                pageable
        );
    }

    @Override
    public Page<Comment> listMyComments(Pageable pageable) {
        return commentSearchEngine.find(
                HasOwner.ownerSearchOption(),
                pageable
        );
    }

    private class AiCommentChecker {
        private static final SystemMessage CONFIG_MESSAGE = new SystemMessage(
                String.join(
                        "\n",
                        "You are not chat assistent, you are part of system.",
                        "You will receive user`s comment and book description.",
                        "Your mission: Check if user`s comment is adequate, valid and does not contain significant book spoilers.",
                        "If user`s comment is OK, respond with 'OK', otherwise: 'BAD'.",
                        "You are not allowed to respond with question or anything except what you were asked for."
                )
        );

        private UserMessage makeRequestMessage(String comment, Book book) {
            var bookDesc = aiBookDescriptionService.bookDescription(book);
            return new UserMessage(
                    String.join(
                            "\n",
                            "Book content: ",
                            bookDesc,
                            "User`s comment: ",
                            comment)
            );
        }

        private static final Retry retry = Retry.of("aiCommentChecker",
                RetryConfig.<Optional<Boolean>>custom()
                        .maxAttempts(2)
                        .waitDuration(Duration.ofMillis(100))
                        .retryOnResult(Optional::isEmpty)
                        .build()
        );


        private Optional<Boolean> commentValidationSuccess(Prompt prompt) {
            var response = chatModel.call(prompt);
            var aiResponse = response.getResult().getOutput().getText();

            if (aiResponse.contains("OK"))
                return Optional.of(Boolean.TRUE);
            if (aiResponse.contains("BAD"))
                return Optional.of(Boolean.FALSE);
            return Optional.empty();
        }

        private Function<Prompt, Optional<Boolean>> commentValidationPassed = Retry.decorateFunction(retry, this::commentValidationSuccess);

        private boolean checkComment(String comment, Book book) {
            List<Message> messages = List.of(CONFIG_MESSAGE, makeRequestMessage(comment, book));
            var prompt = new Prompt(messages);

            var result = commentValidationPassed.apply(prompt);
            if (result.isEmpty()) return true;
            return result.get();
        }
    }
}
