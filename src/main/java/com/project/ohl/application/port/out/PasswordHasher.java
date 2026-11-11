package com.project.ohl.application.port.out;

public interface PasswordHasher {
    String hash(String password);
}
