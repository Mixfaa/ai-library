package com.mixfa.ailibrary.misc;

import lombok.extern.slf4j.Slf4j;

import java.util.Locale;
import java.util.ResourceBundle;

@Slf4j
public enum ExceptionType {
    UNKNOWN, // 0
    FILE_NOT_FOUND, //
    INTERNAL_SERVER, //
    BOOK_NOT_FOUND, // bookId - Object
    BOOK_STATUS_NOT_FOUND, // booKStatusId - Object
    LIBRARY_NOT_FOUND, // id - Object
    NO_BOOK_LOCALE, // libId - Object, bookId - Object, locale - Locale
    NO_BOOKS_AVAILABLE, // libId - Object, bookid - Object
    COMMENT_NOT_FOUND, //
    INVALID_BOOK_RATE, // rate - Double
    BOOK_ORDER_CANT_BE_CANCELLED, // object status id
    RATE_LIMIT_EXCEEDED, //
    INVALID_COMMENT, //
    ACCESS_DENIED, //
    BOOK_ALREADY_RATED; // bookId - Object, username - String

    private final String TEMPLATE_CODE = this.name().toLowerCase();
    private final String RESOURCE_BUNDLE_NAME = "errors";

    public String format(Locale locale, Object[] args) {
        try {
            var bundle = ResourceBundle.getBundle(RESOURCE_BUNDLE_NAME, locale);
            return bundle.getString(TEMPLATE_CODE).formatted(args);
        } catch (Exception e) {
            log.error(e.getMessage());
            return "Exception: " + this.name();
        }
    }

    public UserFriendlyException make(Object... args) {
        return new UserFriendlyException(this.name(), this, args);
    }

    public UserFriendlyException make() {
        return new UserFriendlyException(this.name(), this, null);
    }

    public static UserFriendlyException accessDenied() {
        return ACCESS_DENIED.make();
    }

    public static UserFriendlyException invalidComment() {
        return INVALID_COMMENT.make();
    }

    public static UserFriendlyException rateLimitExceeded() {
        return RATE_LIMIT_EXCEEDED.make();
    }

    // Static creator methods
    public static UserFriendlyException fileNotFound() {
        return FILE_NOT_FOUND.make();
    }

    public static UserFriendlyException internalServer() {
        return INTERNAL_SERVER.make();
    }

    public static UserFriendlyException bookNotFound(Object bookId) {
        return BOOK_NOT_FOUND.make(Utils.idToStr(bookId));
    }

    public static UserFriendlyException bookStatusNotFound(Object bookStatusId) {
        return BOOK_STATUS_NOT_FOUND.make(Utils.idToStr(bookStatusId));
    }

    public static UserFriendlyException libraryNotFound(String id) {
        return LIBRARY_NOT_FOUND.make(id);
    }

    public static UserFriendlyException noBookLocale(String libId, Object bookId, Locale locale) {
        return NO_BOOK_LOCALE.make(libId, Utils.idToStr(bookId), locale);
    }

    public static UserFriendlyException noBooksAvailable(String libId, Object bookId) {
        return NO_BOOKS_AVAILABLE.make(libId, Utils.idToStr(bookId));
    }

    public static UserFriendlyException commentNotFound() {
        return COMMENT_NOT_FOUND.make();
    }

    public static UserFriendlyException invalidBookRate(Double rate) {
        return INVALID_BOOK_RATE.make(rate);
    }

    public static UserFriendlyException bookAlreadyRated(Object bookId, String username) {
        return BOOK_ALREADY_RATED.make(Utils.idToStr(bookId), username);
    }

    public static UserFriendlyException bookOrderCanBeCancelled(Object bookId) {
        return BOOK_ORDER_CANT_BE_CANCELLED.make(Utils.idToStr(bookId));
    }

}