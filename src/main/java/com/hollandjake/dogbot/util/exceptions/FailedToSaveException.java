package com.hollandjake.dogbot.util.exceptions;

public class FailedToSaveException extends RuntimeException {
    public FailedToSaveException(String message) {
        super(message);
    }

    public FailedToSaveException(String message, Exception exception) {
        super(message, exception);
    }
}
