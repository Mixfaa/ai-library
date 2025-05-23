package com.mixfa.ailibrary.model;

public interface BookPricingPolicy {
    public Money calculatePrice(Book book);
}
