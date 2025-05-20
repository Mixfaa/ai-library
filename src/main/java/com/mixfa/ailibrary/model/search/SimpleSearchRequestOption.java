package com.mixfa.ailibrary.model.search;

import com.mixfa.ailibrary.model.Book;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.query.Criteria;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class SimpleSearchRequestOption implements SearchOption {
    private final Book.SearchRequest req;

    @Override
    public List<AggregationOperation> makePipeline() {
        var criteriaList = new ArrayList<Criteria>(7);

        var title = req.title();
        if (!StringUtils.isBlank(title)) {
            // Search across all locales for the title
            Criteria titleCriteria = Criteria.where(Book.Fields.title).elemMatch(
                    Criteria.where("value").regex(title, "i")
            );
            criteriaList.add(titleCriteria);
        }

        // Authors search
        if (req.authors() != null && req.authors().length > 0) {
            Criteria authorCriteria = Criteria.where(Book.Fields.authors).in((Object[]) req.authors());
            criteriaList.add(authorCriteria);
        }

        // Genres search
        if (req.genres() != null && req.genres().length > 0) {
            Criteria genreCriteria = Criteria.where(Book.Fields.genres).in((Object[]) req.genres());
            criteriaList.add(genreCriteria);
        }

        // Took Count range
        if (req.minTookCount() > 0 || req.maxTookCount() > 0) {
            Criteria tookCountCriteria = Criteria.where(Book.Fields.tookCount);

            if (req.minTookCount() > 0)
                tookCountCriteria.gte(req.minTookCount());
            if (req.maxTookCount() > 0)
                tookCountCriteria.lte(req.maxTookCount());

            criteriaList.add(tookCountCriteria);
        }

        // Read Count range
        if (req.minReadCount() > 0 || req.maxReadCount() > 0) {
            Criteria readCountCriteria = Criteria.where(Book.Fields.readCount);

            if (req.minReadCount() > 0)
                readCountCriteria.gte(req.minReadCount());
            if (req.maxReadCount() > 0)
                readCountCriteria.lte(req.maxReadCount());

            criteriaList.add(readCountCriteria);
        }

        return criteriaList.stream().map(criteria -> (AggregationOperation) _ -> criteria.getCriteriaObject()).toList();
    }
}
