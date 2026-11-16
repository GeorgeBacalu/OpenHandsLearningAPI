package com.project.ohl.domain.exception;

public class MediaStorageException extends RuntimeException {
    public MediaStorageException(Throwable cause) {
        super("The media storage operation could not be completed.", cause);
    }
}
