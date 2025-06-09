package com.mixfa.ailibrary.model.library.pricing_policy;

import com.mixfa.ailibrary.model.library.Book;
import com.mixfa.ailibrary.model.library.BookPricingPolicy;
import com.mixfa.ailibrary.model.finance.Money;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ConstPricePolicy implements BookPricingPolicy {
    private final Money price;

    @Override
    public Money calculatePrice(Book book) {
        return price;
    }
}
