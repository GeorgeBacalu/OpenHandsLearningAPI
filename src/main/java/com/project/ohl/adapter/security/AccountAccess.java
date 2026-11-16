package com.project.ohl.adapter.security;

import com.project.ohl.application.model.Account;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("accountAccess")
public class AccountAccess {
    public boolean owns(Authentication authentication, UUID userId) {
        return authentication != null && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof Account account && account.id().equals(userId);
    }
}
