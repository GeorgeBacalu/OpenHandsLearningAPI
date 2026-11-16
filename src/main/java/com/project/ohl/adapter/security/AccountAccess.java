package com.project.ohl.adapter.security;

import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component("accountAccess")
public class AccountAccess {
    public boolean owns(Authentication authentication, UUID userId) {
        return authentication != null && authentication.isAuthenticated()
                && authentication instanceof JwtAuthenticationToken && userId != null
                && userId.toString().equals(authentication.getName());
    }
}
