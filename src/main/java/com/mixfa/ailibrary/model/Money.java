package com.mixfa.ailibrary.model;

public record Money(
        int currency,
        long amount
) {
    public static Money uah(long amount) {
        return new Money(980, amount);
    }
}
