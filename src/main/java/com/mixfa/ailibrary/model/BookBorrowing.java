package com.mixfa.ailibrary.model;

import com.mixfa.ailibrary.model.user.Account;
import com.mixfa.ailibrary.model.user.HasOwner;
import lombok.With;
import lombok.experimental.FieldNameConstants;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document
@With
@FieldNameConstants
public record BookBorrowing(
        @Id ObjectId id,
        @DBRef Book book,
        String invoiceId,
        Money moneyPaid,
        boolean isPaid,
        @DBRef Account owner,
        Instant borrowedTime,
        Instant returnTime) implements HasOwner {
}
