package com.mixfa.ailibrary.ui.localization;

import com.mixfa.ailibrary.misc.ExceptionType;
import com.mixfa.ailibrary.misc.UserFriendlyException;
import com.mixfa.ailibrary.misc.Utils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Locale;
import java.util.ResourceBundle;

@RequiredArgsConstructor
@Accessors(fluent = true)
public class Localizer {
    @Getter
    private final Locale locale;
    private final ResourceBundle textBundle;
    private final ResourceBundle errorsBundle;

    public String formatError(Exception e) {
        if (e instanceof UserFriendlyException userFriendlyException) {
            var templateCode = userFriendlyException.type().templateCode();
            var template = errorsBundle.getString(templateCode);

            return Utils.fmt(template, userFriendlyException.args());
        }

        return formatError(ExceptionType.unknown());
    }

    public String get(String key) {
        try {
            return textBundle.getString(key);
        } catch (Exception e) {
            return key;
        }
    }

    public String formatGet(String key, Object... args) {
        try {
            var template = textBundle.getString(key);
            return Utils.fmt(template, args);
        } catch (Exception e) {
            return key;
        }
    }
}
