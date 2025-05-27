package com.mixfa.ailibrary.service;

import com.mixfa.ailibrary.model.invoice.InvoiceData;
import com.mixfa.ailibrary.model.invoice.InvoiceStatus;
import com.mixfa.ailibrary.model.Money;

public interface InvoiceProvider {
    InvoiceData createInvoice(Money amount, String desctination);

    InvoiceStatus getInvoiceStatus(String invoiceId);
}
