package com.mixfa.ailibrary.service.finance;

import com.mixfa.ailibrary.model.finance.InvoiceData;
import com.mixfa.ailibrary.model.finance.InvoiceStatus;
import com.mixfa.ailibrary.model.finance.Money;

public interface InvoiceProvider {
    InvoiceData createInvoice(Money money, String desctination);

    InvoiceStatus getInvoiceStatus(String invoiceId);
}
