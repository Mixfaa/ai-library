package com.mixfa.ailibrary.misc;


public class AccessDeniedException extends UserFriendlyException {

    public AccessDeniedException(ExceptionType type, Object[] args) {
        super(type.name(), type, args);
    }
}
