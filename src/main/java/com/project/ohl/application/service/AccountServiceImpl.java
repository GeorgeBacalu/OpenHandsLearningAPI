package com.project.ohl.application.service;

import com.project.ohl.application.model.Account;
import com.project.ohl.application.model.Registration;
import com.project.ohl.application.port.in.AccountService;
import com.project.ohl.application.port.out.PasswordHasher;
import com.project.ohl.application.port.out.RoleRepositoryPort;
import com.project.ohl.application.port.out.SchoolRepositoryPort;
import com.project.ohl.application.port.out.UserRepositoryPort;
import com.project.ohl.domain.exception.AccountConflictException;
import com.project.ohl.domain.exception.AuthenticationFailedException;
import com.project.ohl.domain.exception.InvalidRequestException;
import com.project.ohl.domain.model.Authority;
import com.project.ohl.domain.model.User;
import java.util.Locale;
import java.util.UUID;

public final class AccountServiceImpl implements AccountService {
    private final UserRepositoryPort users;
    private final RoleRepositoryPort roles;
    private final SchoolRepositoryPort schools;
    private final PasswordHasher passwords;
    private final String dummyHash;

    public AccountServiceImpl(UserRepositoryPort users, RoleRepositoryPort roles,
            SchoolRepositoryPort schools, PasswordHasher passwords) {
        this.users = users;
        this.roles = roles;
        this.schools = schools;
        this.passwords = passwords;
        dummyHash = passwords.hash(UUID.randomUUID().toString());
    }

    @Override
    public Account authenticate(String email, String password) {
        var user = users.findByEmail(normalize(email));
        boolean matches = passwords.matches(password, user.map(User::getPasswordHash).orElse(dummyHash));
        if (user.isEmpty() || !matches) throw new AuthenticationFailedException();
        return Account.from(user.get());
    }

    @Override
    public Account register(Registration registration) {
        String email = normalize(registration.email());
        if (users.findByEmail(email).isPresent()) throw new AccountConflictException();
        var role = roles.findAll().stream().filter(value -> value.getAuthority() == Authority.USER)
                .findFirst().orElseThrow(() -> new IllegalStateException("The USER role must be configured."));
        var school = schools.findById(registration.schoolId())
                .orElseThrow(() -> new InvalidRequestException("Select an existing school."));
        var user = User.builder().name(registration.name().strip()).email(email)
                .phone(registration.phone()).passwordHash(passwords.hash(registration.password()))
                .school(school).role(role).build();
        return Account.from(users.save(user));
    }

    @Override
    public Account current(UUID id) {
        return Account.from(users.findById(id).orElseThrow(AuthenticationFailedException::new));
    }

    private static String normalize(String email) {
        return email == null ? "" : email.strip().toLowerCase(Locale.ROOT);
    }
}
