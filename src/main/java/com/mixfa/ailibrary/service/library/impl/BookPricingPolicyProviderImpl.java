package com.mixfa.ailibrary.service.library.impl;

import com.mixfa.ailibrary.model.library.Book;
import com.mixfa.ailibrary.model.library.BookPricingPolicy;
import com.mixfa.ailibrary.model.library.pricing_policy.ConstPricePolicy;
import com.mixfa.ailibrary.service.library.BookPricingPolicyProvider;
import org.springframework.stereotype.Service;

@Service
public class BookPricingPolicyProviderImpl implements BookPricingPolicyProvider {
    private final ConstPricePolicy CONST_PRICE_POLICY = new ConstPricePolicy();
    @Override
    public BookPricingPolicy getBookPricingPolicy(Book book) {
        return CONST_PRICE_POLICY;
    }
}
