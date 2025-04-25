package com.mixfa.ailibrary.service.impl;

import com.mixfa.ailibrary.misc.ExceptionType;
import com.mixfa.ailibrary.misc.Utils;
import com.mixfa.ailibrary.model.Comment;
import com.mixfa.ailibrary.model.user.Account;
import com.mixfa.ailibrary.model.user.HasOwner;
import com.mixfa.ailibrary.service.BookService;
import com.mixfa.ailibrary.service.CommentService;
import com.mixfa.ailibrary.service.SearchEngine;
import com.mixfa.ailibrary.service.repo.BookRepo;
import com.mixfa.ailibrary.service.repo.CommentsRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

import static com.mixfa.ailibrary.misc.Utils.fmt;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {
    private final CommentsRepo commentsRepo;
    private final SearchEngine.ForComments commentSearchEngine;
    private final BookRepo bookRepo;
    private final MongoTemplate mongoTemplate;
    private final BookService bookService;

    @Override
    public Comment addComment(Comment.AddRequest request) {
        if (!Utils.inBound(0.0, 5.0, request.rate())) throw ExceptionType.INVALID_BOOK_RATE.make(request.rate());

        var remoteUser = Account.getAuthenticatedAccount();

        if (commentsRepo.existsByBookIdAndOwner(request.bookId(), remoteUser))
            throw ExceptionType.BOOK_ALREADY_RATED.make(request.bookId(), remoteUser.getUsername());

        if (!bookRepo.existsById(request.bookId()))
            throw ExceptionType.BOOK_NOT_FOUND.make(request.bookId());

        var book = bookService.findBookOrThrow(request.bookId());
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
                () -> List.of(Aggregation.match(Criteria.where(fmt("{0}.$id", Comment.Fields.book)).is(Utils.idToObj(bookId)))),
                pageable
        );
    }

    @Override
    public Page<Comment> listMyComments(Pageable pageable) {
        return commentSearchEngine.find(
                () -> List.of(Aggregation.match(HasOwner.ownerCriteria())),
                pageable
        );
    }
}
