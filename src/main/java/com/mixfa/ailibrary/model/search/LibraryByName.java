package com.mixfa.ailibrary.model.search;

import com.mixfa.ailibrary.model.Library;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.query.Criteria;

import java.util.List;

@RequiredArgsConstructor
public class LibraryByName extends SearchOption.SimpleBase {
    private final String query;

    @Override
    public List<AggregationOperation> pipeline() {
        return StringUtils.isBlank(query) ? List.of() : List.of(
                Aggregation.match(
                        Criteria.where(Library.Fields.name).regex(query, "i")
                )
        );
    }
}
