package com.mixfa.ailibrary.model.pricing_policy;

import com.mixfa.ailibrary.model.Book;
import com.mixfa.ailibrary.model.BookPricingPolicy;
import com.mixfa.ailibrary.model.Money;

public class ConstPricePolicy implements BookPricingPolicy {
    private final Money DEFAULT_PRICE = Money.uah(50 * 100);

    @Override
    public Money calculatePrice(Book book) {
        return DEFAULT_PRICE;
    }
}
