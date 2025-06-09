package com.mixfa.ailibrary.misc;

import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.util.Locale;
import java.util.ResourceBundle;


@Slf4j
@Accessors(fluent = true)
public enum ExceptionType {
    UNKNOWN(true), // 0
    FILE_NOT_FOUND(true), //
    INTERNAL_SERVER(true), //
    BOOK_NOT_FOUND, // bookId - Object
    BOOK_STATUS_NOT_FOUND, // booKStatusId - Object
    LIBRARY_NOT_FOUND, // id - Object
    NO_BOOKS_AVAILABLE, // libId - Object, bookid - Object
    COMMENT_NOT_FOUND(true), //
    INVALID_BOOK_RATE, // rate - Double
    RATE_LIMIT_EXCEEDED(true), //
    INVALID_COMMENT(true), //
    ACCESS_DENIED(true), //
    BOOK_ALREADY_BORROWED, // string
    CURRENCY_CONVERTION_FAILED(true),
    INVOICE_CREATION_FAILED(true),
    USERNAME_CANNOT_BE_BLANK(true),
    BOOK_ALREADY_RATED; // bookId - Object, username - String

    private final UserFriendlyException instance;
    @Getter
    private final String templateCode = this.name().toLowerCase();

    ExceptionType() {
        this.instance = null;
    }

    ExceptionType(boolean instanciate) {
        this.instance = instanciate ? this.make() : null;
    }

    public UserFriendlyException make(Object... args) {
        return new UserFriendlyException(this.templateCode, this, args);
    }

    public UserFriendlyException make() {
        return new UserFriendlyException(this.templateCode, this, null);
    }

    public static UserFriendlyException unknown() {
        return UNKNOWN.instance;
    }

    public static UserFriendlyException currencyConvertionFailed() {
        return CURRENCY_CONVERTION_FAILED.instance;
    }

    public static UserFriendlyException invoiceCreationFailed() {
        return INVOICE_CREATION_FAILED.instance;
    }

    public static UserFriendlyException accessDenied() {
        return ACCESS_DENIED.instance;
    }

    public static UserFriendlyException invalidComment() {
        return INVALID_COMMENT.instance;
    }

    public static UserFriendlyException rateLimitExceeded() {
        return RATE_LIMIT_EXCEEDED.instance;
    }

    public static UserFriendlyException fileNotFound() {
        return FILE_NOT_FOUND.instance;
    }

    public static UserFriendlyException internalServer() {
        return INTERNAL_SERVER.instance;
    }

    public static UserFriendlyException commentNotFound() {
        return COMMENT_NOT_FOUND.instance;
    }

    public static UserFriendlyException bookAleardyBorrowed(Object bookId) {
        return BOOK_ALREADY_BORROWED.make(bookId);
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

    public static UserFriendlyException noBooksAvailable(String libId, Object bookId) {
        return NO_BOOKS_AVAILABLE.make(libId, Utils.idToStr(bookId));
    }

    public static UserFriendlyException invalidBookRate(Double rate) {
        return INVALID_BOOK_RATE.make(rate);
    }

    public static UserFriendlyException bookAlreadyRated(Object bookId, String username) {
        return BOOK_ALREADY_RATED.make(Utils.idToStr(bookId), username);
    }
}