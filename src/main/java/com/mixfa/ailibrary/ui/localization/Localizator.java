package com.mixfa.ailibrary.ui.localization;

import com.mixfa.ailibrary.misc.Utils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.checkerframework.checker.units.qual.Acceleration;

import java.util.Locale;
import java.util.ResourceBundle;

@RequiredArgsConstructor
@Accessors(fluent = true)
public class Localizator {
    @Getter
    private final Locale locale;
    private final ResourceBundle bundle;

    public String get(String key) {
        return bundle.getString(key);
    }

    public String formatGet(String key, Object... args) {
        var template = bundle.getString(key);
        return Utils.fmt(template, args);
    }
}
