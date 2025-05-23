package com.mixfa.ailibrary.service;

import com.mixfa.ailibrary.model.Book;
import com.mixfa.ailibrary.model.BookPricingPolicy;

public interface BookPricingPolicyProvider {
    public BookPricingPolicy getBookPricingPolicy(Book book);
}
