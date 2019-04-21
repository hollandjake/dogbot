package com.hollandjake.dogbot.util.exceptions;

public class MissingModuleException extends RuntimeException {
    public MissingModuleException(String message) {
        super(message);
    }
}
