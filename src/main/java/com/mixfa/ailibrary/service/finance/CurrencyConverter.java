package com.mixfa.ailibrary.service.finance;

import com.mixfa.ailibrary.model.finance.Money;

import java.util.Currency;

public interface CurrencyConverter {
    Money convert(Money from, Currency to);
}
