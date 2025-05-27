package com.mixfa.ailibrary.service.impl;

import com.mixfa.ailibrary.model.statistics.StatisticsRecord;
import com.mixfa.ailibrary.service.CurrencyConverter;
import com.mixfa.ailibrary.service.SearchEngine;
import com.mixfa.ailibrary.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {
    private final CurrencyConverter currencyConverter;
    private final SearchEngine.ForBorrowings borrowingsSearchEngine;
    private final MongoTemplate mongoTemplate;

    @Override
    public StatisticsRecord getStatistics(Instant from, Instant to, int targetCurrency) {

    }
}
