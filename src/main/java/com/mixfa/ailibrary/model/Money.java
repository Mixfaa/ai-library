package com.mixfa.ailibrary.model;

import com.mixfa.ailibrary.misc.Utils;
import lombok.experimental.FieldNameConstants;
import org.springframework.data.annotation.PersistenceCreator;

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

//    @PersistenceCreator
//    public Money(String currency, long amount) {
//        this(Currency.getInstance(currency), amount);
//    }

    public String asString() {
        return Utils.calculateCurrency(amount, currency) + currency.getSymbol();
    }
}
