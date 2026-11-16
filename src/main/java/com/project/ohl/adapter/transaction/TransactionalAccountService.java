package com.project.ohl.adapter.transaction;

import com.project.ohl.application.model.Account;
import com.project.ohl.application.model.Registration;
import com.project.ohl.application.port.in.AccountService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TransactionalAccountService implements AccountService {
    private final AccountService delegate;

    @Override
    public Account authenticate(String email, String password) {
        return delegate.authenticate(email, password);
    }

    @Override
    @Transactional
    public Account register(Registration registration) {
        return delegate.register(registration);
    }

    @Override
    public Account current(UUID id) {
        return delegate.current(id);
    }

    @Override
    @Transactional
    public void updatePassword(UUID id, String currentPassword, String newPassword) {
        delegate.updatePassword(id, currentPassword, newPassword);
    }

    @Override
    @Transactional
    public void deleteAccount(UUID id, String currentPassword) {
        delegate.deleteAccount(id, currentPassword);
    }
}
