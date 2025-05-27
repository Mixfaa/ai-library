package com.mixfa.ailibrary.service;

import com.mixfa.ailibrary.model.Money;

public interface CurrencyConverter {
    Money convert(Money from, Money to);
}
