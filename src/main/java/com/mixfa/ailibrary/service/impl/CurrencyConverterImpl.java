package com.mixfa.ailibrary.service.impl;

import com.mixfa.ailibrary.misc.ExceptionType;
import com.mixfa.ailibrary.misc.Utils;
import com.mixfa.ailibrary.model.Money;
import com.mixfa.ailibrary.service.CurrencyConverter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.util.Currency;

@Slf4j
@Service
public class CurrencyConverterImpl implements CurrencyConverter {
    private final String apiKey;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public CurrencyConverterImpl(@Value("${currencyconverter.api-key}") String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    public Money convert(Money from, Currency targetCurrency) {
        if (from.currency().getNumericCode() == targetCurrency.getNumericCode())
            return from;

        var originCurrency = from.currency();

        var uri = Utils.fmt("https://v6.exchangerate-api.com/v6/{0}/pair/{1}/{2}", apiKey, originCurrency.getCurrencyCode(), targetCurrency.getCurrencyCode());
        var ratesRequest = HttpRequest.newBuilder(URI.create(uri)).GET().build();

        try {
            var response = httpClient.send(ratesRequest, Utils.mapBodyHandler());
            if (response.statusCode() != 200) {
                log.error("Error response from API: {}", response.statusCode());
                throw ExceptionType.currencyConvertionFailed();
            }

            var ratio = (Double) response.body().get("conversion_rate");

            return new Money(targetCurrency, Math.round(from.amount() * ratio.doubleValue()));
        } catch (Exception e) {
            log.error("Failed to get currency pair conversion ratio", e);
            throw ExceptionType.currencyConvertionFailed();
        }
    }
}
