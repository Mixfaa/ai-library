package com.mixfa.ailibrary.service.library;


import com.mixfa.ailibrary.model.library.Comment;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CommentService {
    Comment addComment(Comment.AddRequest request);

    void removeComment(Object commentId);

    double getBookRate(Object bookId);

    Page<Comment> listComments(Object bookId, Pageable pageable);

    Page<Comment> listMyComments(Pageable pageable);

    public static sealed interface Event {
        public static record OnCommentAdded(Comment comment, double newRate) implements Event {
        }

        public static record OnCommentRemoved(Comment comment, double newRate) implements Event {
        }
    }

    @ConfigurationProperties(prefix = "commentservice")
    public record Properties(
            CommentsChecking commentschecking
    ) {
        public record CommentsChecking(
                boolean enabled
        ) {
        }
    }
}
