package com.mixfa.ailibrary.model.library.pricing_policy;

import com.mixfa.ailibrary.model.library.Book;
import com.mixfa.ailibrary.model.library.BookPricingPolicy;
import com.mixfa.ailibrary.model.finance.Money;

public class ConstPricePolicy implements BookPricingPolicy {
    private final Money DEFAULT_PRICE = Money.uah(150 * 100);

    @Override
    public Money calculatePrice(Book book) {
        return DEFAULT_PRICE;
    }
}
