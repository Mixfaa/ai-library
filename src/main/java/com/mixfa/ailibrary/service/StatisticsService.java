package com.mixfa.ailibrary.service;

import com.mixfa.ailibrary.model.statistics.StatisticRecord;

import java.time.LocalDate;
import java.util.Currency;

public interface StatisticsService {
    StatisticRecord getStatistics(LocalDate from, LocalDate to);
}
