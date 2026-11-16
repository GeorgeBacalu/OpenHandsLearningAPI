package com.project.ohl.domain.exception;

public final class AuthenticationFailedException extends RuntimeException {
    public AuthenticationFailedException() { super("Invalid email or password."); }
}
