package com.project.ohl.domain.exception;

public final class AccountConflictException extends RuntimeException {
    public AccountConflictException() { super("An account with this email already exists."); }
}
