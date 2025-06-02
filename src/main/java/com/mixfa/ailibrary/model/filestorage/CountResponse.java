package com.mixfa.ailibrary.model.filestorage;

import lombok.experimental.FieldNameConstants;

@FieldNameConstants
public record CountResponse(
        long count
) {
    public static final CountResponse ZERO = new CountResponse(0);
}
