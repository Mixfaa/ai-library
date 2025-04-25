package com.mixfa.ailibrary.misc;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Locale;

public class MongoLocaleConverter {

    @Component
    @ReadingConverter
    public static class StringToLocaleConverter implements Converter<String, Locale> {
        @Override
        public Locale convert(String source) {
            String[] parts = source.split("_", -1); // Use -1 limit to handle trailing separators
            if (parts.length == 1) {
                return new Locale(parts[0]);
            } else if (parts.length == 2) {
                return new Locale(parts[0], parts[1]);
            } else if (parts.length >= 3) {
                return new Locale(parts[0], parts[1], parts[2]);
            } else {
                // Handle invalid format if necessary, maybe return null or throw exception
                return null; // Or throw new IllegalArgumentException("Invalid locale format: " + source);
            }
        }
    }

    @Component
    @WritingConverter
    public static class LocaleToStringConverter implements Converter<Locale, String> {
        @Override
        public String convert(Locale source) {
            return source.toLanguageTag();
        }
    }

}
