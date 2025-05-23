package com.mixfa.ailibrary.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mixfa.ailibrary.misc.Utils;
import com.mixfa.ailibrary.model.Money;
import com.mixfa.ailibrary.service.InvoiceProvider;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.util.Map;

@Slf4j
@Service
public class MonoInvoiceProvider implements InvoiceProvider {
    private final String xToken;
    private final ObjectMapper objectMapper;
    private static final String BASE_URL = "https://api.monobank.ua";
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public MonoInvoiceProvider(@Value("${mono.xToken}") String xToken, ObjectMapper objectMapper) {
        this.xToken = xToken;
        this.objectMapper = objectMapper;
    }

    private static URI makeUri(String uri) {
        return URI.create(BASE_URL + uri);
    }

    private HttpRequest.Builder configureRequest(HttpRequest.Builder reqBuilder) {
        return reqBuilder.header("X-Token", xToken);
    }

    @SneakyThrows
    @Override
    public InvoiceData createInvoice(Money amount, String destination) {
        var data = objectMapper.writeValueAsBytes(
                Map.of(
                        "amount", amount.amount(),
                        "ccy", amount.currency(),
                        "merchantPaymInfo", Map.of("destination", destination)
                )
        );

        var request = configureRequest(HttpRequest.newBuilder(makeUri("/api/merchant/invoice/create")))
                .POST(HttpRequest.BodyPublishers.ofByteArray(data))
                .build();

        var response = httpClient.send(request, Utils.mapBodyHandler());
        if (response.statusCode() != 200) {
            log.error("Error creating invoice: {}", response.statusCode());
            throw new RuntimeException("Error creating invoice");
        }

        var responseBody = response.body();

        return new InvoiceData(
                (String) responseBody.get("invoiceId"),
                (String) responseBody.get("pageUrl")
        );
    }

    @Override
    @SneakyThrows
    public InvoiceStatus getInvoiceStatus(String invoiceId) {
        var request = configureRequest(HttpRequest.newBuilder(makeUri("/api/merchant/invoice/status?invoiceId=" + invoiceId)))
                .GET()
                .build();
        var response = httpClient.send(request, Utils.mapBodyHandler());

        if (response.statusCode() != 200) {
            log.error("Error getting invoice status: {}", response.statusCode());
            throw new RuntimeException("Error getting invoice status");
        }

        var responseBody = response.body();
        return InvoiceStatus.valueOf(responseBody.get("status").toString());
    }
}
