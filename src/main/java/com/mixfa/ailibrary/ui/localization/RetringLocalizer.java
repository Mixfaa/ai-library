package com.mixfa.ailibrary.ui.localization;


import java.util.Locale;
import java.util.ResourceBundle;

public class RetringLocalizer extends Localizer {
    private final Localizer fallbackLocalizer;

    public RetringLocalizer(Locale locale, ResourceBundle textBundle, ResourceBundle errorsBundle, Localizer fallbackLocalizer) {
        super(locale, textBundle, errorsBundle);
        this.fallbackLocalizer = fallbackLocalizer;
    }

    @Override
    public String formatError(Exception e) {
        try {
            return super.formatError(e);
        } catch (Exception ex) {
            return fallbackLocalizer.formatError(ex);
        }
    }

    @Override
    public String get(String key) {
        try {
            return super.get(key);
        } catch (Exception ex) {
            return fallbackLocalizer.get(key);
        }
    }

    @Override
    public String formatGet(String key, Object... args) {
        try {
            return super.formatGet(key, args);
        } catch (Exception ex) {
            return fallbackLocalizer.formatGet(key, args);
        }
    }
}
