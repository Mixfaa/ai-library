package com.mixfa.ailibrary.service.impl;

import com.mixfa.ailibrary.misc.Utils;
import com.mixfa.ailibrary.model.Money;
import com.mixfa.ailibrary.service.CurrencyConverter;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;

@Service
public class CurrencyConverterImpl implements CurrencyConverter {
    private final String apiKey;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public CurrencyConverterImpl(@Value("${currencyconverter.api-key}") String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    @SneakyThrows
    public Money convert(Money from, int targetCurrency) {
        if (from.currency() == targetCurrency)
            return from;

        var targetCurrencySymbol = Utils.findCurrencyByCodeOrThrow(targetCurrency);
        var originCurrencySymbol = Utils.findCurrencyByCodeOrThrow(from.currency());

        var uri = Utils.fmt("https://v6.exchangerate-api.com/v6/{0}/pair/{1}/{2}", apiKey, originCurrencySymbol, targetCurrencySymbol);
        var ratesRequest = HttpRequest.newBuilder(URI.create(uri)).GET().build();

        var response = httpClient.send(ratesRequest, Utils.mapBodyHandler());
        if (response.statusCode() != 200)
            throw new RuntimeException("Failed to get currency pair conversion ratio");

        var ratio = (Double) response.body().get("conversion_rate");

        return new Money(targetCurrency, Math.round(from.amount() * ratio.doubleValue()));
    }
}
