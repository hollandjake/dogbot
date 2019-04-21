package com.hollandjake.dogbot.util.exceptions;

public class FailedToFetchException extends RuntimeException {
    public FailedToFetchException(String message) {
        super(message);
    }

    public FailedToFetchException(String message, Exception exception) {
        super(message, exception);
    }
}
