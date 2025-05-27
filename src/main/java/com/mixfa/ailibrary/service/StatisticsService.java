package com.mixfa.ailibrary.service;

import com.mixfa.ailibrary.model.statistics.StatisticsRecord;

import java.time.Instant;

public interface StatisticsService {
    StatisticsRecord getStatistics(Instant from, Instant to, int targetCurrency);
}
