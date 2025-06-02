package com.mixfa.ailibrary.service.statistic;

import com.mixfa.ailibrary.model.statistics.StatisticRecord;

import java.time.LocalDate;

public interface StatisticsService {
    StatisticRecord getStatistics(LocalDate from, LocalDate to);
}
