package com.mixfa.ailibrary.service;

import com.mixfa.ailibrary.model.Money;

public interface InvoiceProvider {
    InvoiceData createInvoice(Money amount, String desctination);

    InvoiceStatus getInvoiceStatus(String invoiceId);

    enum InvoiceStatus {
        created,
        processing,
        hold,
        success,
        failure,
        reversed,
        expired;
    }

    record InvoiceData(
            String invoiceId,
            String pageUrl
    ) {
    }
}
