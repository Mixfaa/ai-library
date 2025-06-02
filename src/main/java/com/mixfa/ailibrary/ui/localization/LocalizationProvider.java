package com.mixfa.ailibrary.ui.localization;

import com.mixfa.ailibrary.service.user.UserDataService;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;

@UtilityClass
public class LocalizationProvider {
    private static final String TEXT_BUNDLE_NAME = "localization";
    private static final String ERROR_BUNDLE_NAME = "errors";
    private static final ResourceBundle ENGLISH_TEXT_BUNDLE = ResourceBundle.getBundle(TEXT_BUNDLE_NAME);
    private static final ResourceBundle ENGLISH_ERROR_BUNDLE = ResourceBundle.getBundle(ERROR_BUNDLE_NAME);

    private static final Localizer ENGLISH_LOCALIZER = new Localizer(Locale.ENGLISH, ENGLISH_TEXT_BUNDLE, ENGLISH_ERROR_BUNDLE);
    private static final Map<Locale, Localizer> BUNDLES = new ConcurrentHashMap<>();

    private static Localizer getLocalizator(Locale locale) {
        if (locale.equals(Locale.ENGLISH)) return ENGLISH_LOCALIZER;

        var bundle = BUNDLES.computeIfAbsent(locale, lkey -> {
            try {
                return new Localizer(lkey,
                        ResourceBundle.getBundle(TEXT_BUNDLE_NAME, lkey),
                        ResourceBundle.getBundle(ERROR_BUNDLE_NAME, lkey));
            } catch (MissingResourceException e) {
                return null;
            }
        });

        return bundle == null ? ENGLISH_LOCALIZER : bundle;
    }

    public static Localizer getLocalizator() {
        var locale = Initializer.getInstance().getUserDataService().getLocale();
        return getLocalizator(locale);
    }

    @Getter
    @Component
    public static class Initializer {
        private final UserDataService userDataService;
        @Getter
        private volatile static Initializer instance;

        public Initializer(UserDataService userDataService) {
            this.userDataService = userDataService;
            this.instance = this;
        }
    }
}
