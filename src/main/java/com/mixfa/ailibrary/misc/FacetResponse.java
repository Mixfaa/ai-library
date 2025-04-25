package com.mixfa.ailibrary.misc;

import com.mixfa.ailibrary.model.CountResponse;

import java.util.List;

public interface FacetResponse {
    List elements();
    List<CountResponse> count();
}