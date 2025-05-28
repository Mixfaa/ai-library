package com.mixfa.ailibrary.model;

import com.mixfa.ailibrary.misc.Utils;

public record Money(
        int currency,
        long amount
) {
    public static Money uah(long amount) {
        return new Money(980, amount);
    }

    public String asString() {
        var currency = Utils.findCurrencyByCodeOrThrow(currency());
        return Utils.calculateCurrency(amount, currency) + currency.getSymbol();
    }
}
