package com.mixfa.ailibrary.misc;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.http.HttpResponse;


class JsonMappingBodyHandler<T> implements HttpResponse.BodyHandler<T> {
    private final TypeReference<T> targetType;
    private final ObjectMapper objectMapper;

    public JsonMappingBodyHandler(TypeReference<T> targetType, ObjectMapper objectMapper) {
        this.targetType = targetType;
        this.objectMapper = objectMapper;
    }

    @Override
    public HttpResponse.BodySubscriber<T> apply(HttpResponse.ResponseInfo responseInfo) {
        return new JsonMappingBodySubscriber<>(targetType, objectMapper);
    }
}