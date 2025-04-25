package com.mixfa.ailibrary.service.impl;

import com.mixfa.ailibrary.misc.FacetResponse;
import com.mixfa.ailibrary.model.CountResponse;
import com.mixfa.ailibrary.model.search.SearchOption;
import com.mixfa.ailibrary.service.SearchEngine;
import jakarta.annotation.Nullable;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.type.TypeDescription;
import org.bson.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.CriteriaDefinition;
import org.springframework.data.mongodb.util.BsonUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@UtilityClass
@SuppressWarnings("unchecked")
class RuntimeFacetResponseClassGenerator {
    private static final Map<Class, Class<FacetResponse>> cache = new ConcurrentHashMap<>();

    @SneakyThrows
    public static <T> Class<FacetResponse> get(Class<T> typeClass) {
        Class<FacetResponse> cached = cache.get(typeClass);
        if (cached == null) {
            var elementsTypeDef = TypeDescription.Generic.Builder
                    .parameterizedType(List.class, typeClass)
                    .build();
            var countTypeDef = TypeDescription.Generic.Builder
                    .parameterizedType(List.class, CountResponse.class)
                    .build();

            cached = (Class<FacetResponse>) new ByteBuddy()
                    .makeRecord()
                    .implement(FacetResponse.class)
                    .name("FacetResponse" + typeClass.getSimpleName())
                    .defineRecordComponent("elements", elementsTypeDef)
                    .defineRecordComponent("count", countTypeDef)
                    .make()
                    .load(FacetResponse.class.getClassLoader())
                    .getLoaded();

            cache.put(typeClass, cached);
        }

        return cached;
    }
}

@Slf4j
public final class GenericSearchEngineImpl<T> implements SearchEngine<T> {
    private final MongoTemplate mongoTemplate;
    private final Class<T> typeClass;

    private final ProjectionOperation projectionOperation;

    public GenericSearchEngineImpl(MongoTemplate mongoTemplate, Class<T> typeClass) {
        this.mongoTemplate = mongoTemplate;
        this.typeClass = typeClass;
        this.projectionOperation = Aggregation.project(typeClass);
    }

    private static class EmptyCriteriaDef implements CriteriaDefinition {

        @Override
        public Document getCriteriaObject() {
            return BsonUtils.EMPTY_DOCUMENT;
        }

        @Override
        public String getKey() {
            return null;
        }

        static final EmptyCriteriaDef instance = new EmptyCriteriaDef();
    }

    private Page<T> handleFacetResponse(FacetResponse facetResponse, Pageable pageable) {
        try {
            if (facetResponse == null)
                return Page.empty(pageable);
            var count = facetResponse.count().getFirst().count();
            return new PageImpl<>(facetResponse.elements(), pageable, count);
        } catch (Exception ex) {
            // mostly NoSuchElementException
            return Page.empty(pageable);
        }
    }

    @Override
    public Page<T> findAll(Pageable pageable) {
        var match = Aggregation.match(EmptyCriteriaDef.instance);
        SkipOperation skip;
        LimitOperation limit;
        if (pageable.isUnpaged()) {
            skip = Aggregation.skip(0);
            limit = Aggregation.limit(15);
        } else {
            skip = Aggregation.skip(pageable.getOffset());
            limit = Aggregation.limit(pageable.getPageSize());
        }

        var facet = Aggregation.facet(match, skip, limit)
                .as("elements")
                .and(Aggregation.count().as(CountResponse.Fields.count))
                .as("count");

        var facetResponseClass = RuntimeFacetResponseClassGenerator.get(typeClass);

        var result = mongoTemplate.aggregate(Aggregation.newAggregation(facet), typeClass, facetResponseClass)
                .getUniqueMappedResult();
        return handleFacetResponse(result, pageable);
    }

    @Override
    public Page<T> find(SearchOption searchOptions, Pageable pageable) {
        var optionsPipeline = searchOptions.makePipeline();
        List<AggregationOperation> pipeline = new ArrayList<>(optionsPipeline.size() + 3);
        pipeline.addAll(optionsPipeline);

        var countPipeline = new ArrayList<>(pipeline);
        countPipeline.addLast(Aggregation.count().as(CountResponse.Fields.count));

        SkipOperation skip;
        LimitOperation limit;
        if (pageable.isUnpaged()) {
            skip = Aggregation.skip(0);
            limit = Aggregation.limit(15);
        } else {
            skip = Aggregation.skip(pageable.getOffset());
            limit = Aggregation.limit(pageable.getPageSize());
        }

        pipeline.addLast(skip);
        pipeline.addLast(limit);

        pipeline.addLast(projectionOperation);

        var sort = pageable.getSort();
        if (!sort.isUnsorted())
            pipeline.addLast(Aggregation.sort(sort));

        var facet = Aggregation.facet(pipeline.toArray(AggregationOperation[]::new))
                .as("elements")
                .and(countPipeline.toArray(AggregationOperation[]::new))
                .as("count");
        var facetResponseClass = RuntimeFacetResponseClassGenerator.get(typeClass);

        var result = mongoTemplate.aggregate(Aggregation.newAggregation(facet), typeClass, facetResponseClass);
        return handleFacetResponse(result.getUniqueMappedResult(), pageable);
    }

    @Nullable
    @Override
    public T findOne(SearchOption searchOption) {
        return find(searchOption, PageRequest.of(0, 1)).getContent().getFirst();
    }
}
