package com.mixfa.ailibrary.model.search;

import com.mixfa.ailibrary.model.Comment;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.query.Criteria;

import java.util.List;

import static com.mixfa.ailibrary.misc.Utils.fmt;

public class CommentsByBook extends SearchOption.SimpleBase {
    private final List<AggregationOperation> pipeline;

    public CommentsByBook(ObjectId bookId) {
        if (bookId == null)
            this.pipeline = List.of();
        else
            this.pipeline = List.of(Aggregation.match(Criteria.where(fmt("{0}.$id", Comment.Fields.book)).is(bookId)));
    }

    @Override
    List<AggregationOperation> pipeline() {
        return pipeline;
    }
}
