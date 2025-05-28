package com.mixfa.ailibrary.service;

import com.mixfa.ailibrary.model.Money;

import java.util.Currency;

public interface CurrencyConverter {
    Money convert(Money from, Currency to);
}
