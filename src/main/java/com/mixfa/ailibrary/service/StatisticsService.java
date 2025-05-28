package com.mixfa.ailibrary.service;

import com.mixfa.ailibrary.model.statistics.StatisticsRecord;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Currency;

public interface StatisticsService {
    StatisticsRecord getStatistics(LocalDate from, LocalDate to, Currency targetCurrency);
}
