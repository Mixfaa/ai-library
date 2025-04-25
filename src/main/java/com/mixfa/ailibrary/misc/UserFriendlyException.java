package com.mixfa.ailibrary.misc;

import lombok.Getter;

import java.util.Locale;

@Getter
public class UserFriendlyException extends RuntimeException {
    private final ExceptionType type;
    private final Object[] args;

    public UserFriendlyException(String message, ExceptionType type, Object[] args) {
        super(message);
        this.type = type;
        this.args = args;
    }

    public String format(Locale locale) {
        return type.format(locale, args);
    }

    public UserFriendlyException(String message, Throwable cause, ExceptionType type, Object[] args) {
        super(message, cause);
        this.type = type;
        this.args = args;
    }
}
