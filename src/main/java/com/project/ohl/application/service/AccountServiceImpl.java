package com.project.ohl.application.service;

import com.project.ohl.application.model.Account;
import com.project.ohl.application.model.Registration;
import com.project.ohl.application.port.in.AccountService;
import com.project.ohl.application.port.out.PasswordHasher;
import com.project.ohl.application.port.out.SchoolRepositoryPort;
import com.project.ohl.application.port.out.UserRepositoryPort;
import com.project.ohl.domain.exception.AccountConflictException;
import com.project.ohl.domain.exception.AuthenticationFailedException;
import com.project.ohl.domain.exception.InvalidRequestException;
import com.project.ohl.domain.model.Role;
import com.project.ohl.domain.model.User;
import java.util.Locale;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AccountServiceImpl implements AccountService {
    private final UserRepositoryPort users;
    private final SchoolRepositoryPort schools;
    private final PasswordHasher passwords;
    private final String dummyHash;

    public AccountServiceImpl(UserRepositoryPort users,
            SchoolRepositoryPort schools, PasswordHasher passwords) {
        this.users = users;
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
    @Transactional
    public Account register(Registration registration) {
        String email = normalize(registration.email());
        if (users.findByEmail(email).isPresent()) throw new AccountConflictException();
        var school = schools.findById(registration.schoolId())
                .orElseThrow(() -> new InvalidRequestException("Select an existing school."));
        var user = User.builder().name(registration.name().strip()).email(email)
                .phone(registration.phone()).passwordHash(passwords.hash(registration.password()))
                .school(school).role(Role.USER).build();
        return Account.from(users.save(user));
    }

    @Override
    public Account current(UUID id) {
        return Account.from(users.findById(id).orElseThrow(AuthenticationFailedException::new));
    }

    @Override
    @Transactional
    public void updatePassword(UUID id, String currentPassword, String newPassword) {
        var user = verifiedUser(id, currentPassword);
        user.setPasswordHash(passwords.hash(newPassword));
        users.update(user);
    }

    @Override
    @Transactional
    public void deleteAccount(UUID id, String currentPassword) {
        users.delete(verifiedUser(id, currentPassword));
    }

    private User verifiedUser(UUID id, String currentPassword) {
        var user = users.findById(id).orElseThrow(AuthenticationFailedException::new);
        if (!passwords.matches(currentPassword, user.getPasswordHash()))
            throw new InvalidRequestException("The current password is incorrect.");
        return user;
    }

    private static String normalize(String email) {
        return email == null ? "" : email.strip().toLowerCase(Locale.ROOT);
    }
}
