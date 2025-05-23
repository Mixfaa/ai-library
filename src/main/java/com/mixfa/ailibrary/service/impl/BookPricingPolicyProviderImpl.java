package com.mixfa.ailibrary.service.impl;

import com.mixfa.ailibrary.model.Book;
import com.mixfa.ailibrary.model.BookPricingPolicy;
import com.mixfa.ailibrary.model.pricing_policy.ConstPricePolicy;
import com.mixfa.ailibrary.service.BookPricingPolicyProvider;
import org.springframework.stereotype.Service;

@Service
public class BookPricingPolicyProviderImpl implements BookPricingPolicyProvider {
    private final ConstPricePolicy constPrice = new ConstPricePolicy();
    @Override
    public BookPricingPolicy getBookPricingPolicy(Book book) {
        return constPrice;
    }
}
