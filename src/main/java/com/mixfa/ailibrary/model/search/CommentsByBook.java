package com.mixfa.ailibrary.model.search;

import com.mixfa.ailibrary.model.Comment;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.query.Criteria;

import java.util.List;

import static com.mixfa.ailibrary.misc.Utils.fmt;

public class CommentsByBook extends SearchOption.ImmutableAdapter {
    public CommentsByBook(ObjectId bookId) {
        super(makePipeline(bookId));
    }

    public static List<AggregationOperation> makePipeline(ObjectId bookId) {
        if (bookId == null) return List.of();
        return List.of(Aggregation.match(Criteria.where(fmt("{0}.$id", Comment.Fields.book)).is(bookId)));
    }
}
