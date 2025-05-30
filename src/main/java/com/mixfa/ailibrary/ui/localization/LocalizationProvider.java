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
public class LocalizationProvider { // availiable from anywhere
    private static final String BUNDLE_NAME = "localization";
    private static final ResourceBundle ENGLISH_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);
    private static final Localizator ENGLISH_LOCALIZATOR = new Localizator(Locale.ENGLISH, ENGLISH_BUNDLE);
    private static final Map<Locale, Localizator> BUNDLES = new ConcurrentHashMap<>();

    private static Localizator getLocalizator(Locale locale) {
        if (locale.equals(Locale.ENGLISH)) return ENGLISH_LOCALIZATOR;

        var bundle = BUNDLES.computeIfAbsent(locale, lkey -> {
            try {
                return new Localizator(lkey, ResourceBundle.getBundle(BUNDLE_NAME, lkey));
            } catch (MissingResourceException e) {
                return null;
            }
        });

        return bundle == null ? ENGLISH_LOCALIZATOR : bundle;
    }

    public static Localizator getLocalizator() {
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
