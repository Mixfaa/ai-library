package com.mixfa.ailibrary.ui.localization;


import java.util.Locale;
import java.util.ResourceBundle;

public class RetringLocalizator extends Localizator {
    private final Localizator fallbackLocalizator;

    public RetringLocalizator(Locale locale, ResourceBundle textBundle, ResourceBundle errorsBundle, Localizator fallbackLocalizator) {
        super(locale, textBundle, errorsBundle);
        this.fallbackLocalizator = fallbackLocalizator;
    }

    @Override
    public String formatError(Exception e) {
        try {
            return super.formatError(e);
        } catch (Exception ex) {
            return fallbackLocalizator.formatError(ex);
        }
    }

    @Override
    public String get(String key) {
        try {
            return super.get(key);
        } catch (Exception ex) {
            return fallbackLocalizator.get(key);
        }
    }

    @Override
    public String formatGet(String key, Object... args) {
        try {
            return super.formatGet(key, args);
        } catch (Exception ex) {
            return fallbackLocalizator.formatGet(key, args);
        }
    }
}
