package com.mixfa.ailibrary.model.library;

import com.mixfa.ailibrary.model.finance.Money;

public interface BookPricingPolicy {
    public Money calculatePrice(Book book);
}
