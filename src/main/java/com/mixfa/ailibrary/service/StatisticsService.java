package com.mixfa.ailibrary.service;

import com.mixfa.ailibrary.model.statistics.StatisticsRecord;

import java.time.Instant;
import java.time.LocalDate;

public interface StatisticsService {
    StatisticsRecord getStatistics(LocalDate from, LocalDate to, int targetCurrency);
}
