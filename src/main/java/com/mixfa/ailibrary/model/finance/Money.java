package com.mixfa.ailibrary.model.finance;

import com.mixfa.ailibrary.misc.Utils;
import lombok.experimental.FieldNameConstants;

import java.util.Currency;

@FieldNameConstants
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
