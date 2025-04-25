package com.mixfa.ailibrary.service;


import com.mixfa.ailibrary.model.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CommentService {
    Comment addComment(Comment.AddRequest request);

    void removeComment(Object commentId);

    double getBookRate(Object bookId);

    Page<Comment> listComments(Object bookId, Pageable pageable);

    Page<Comment> listMyComments(Pageable pageable);
}
