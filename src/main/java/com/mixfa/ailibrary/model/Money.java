package com.mixfa.ailibrary.model;

import com.mixfa.ailibrary.misc.Utils;

import java.util.Currency;

public record Money(
        Currency currency,
        long amount
) {
    private static final Currency UAH = Currency.getInstance("UAH");

    public static Money uah(long amount) {
        return new Money(UAH, amount);
    }

    public String asString() {
        return Utils.calculateCurrency(amount, currency) + currency.getSymbol();
    }
}
