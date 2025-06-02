package com.mixfa.ailibrary.service.library;

import com.mixfa.ailibrary.model.library.Book;
import com.mixfa.ailibrary.model.library.BookPricingPolicy;

public interface BookPricingPolicyProvider {
    public BookPricingPolicy getBookPricingPolicy(Book book);
}
