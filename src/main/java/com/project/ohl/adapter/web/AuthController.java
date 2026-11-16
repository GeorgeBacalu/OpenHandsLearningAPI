package com.project.ohl.adapter.web;

import com.project.ohl.application.model.Account;
import com.project.ohl.application.model.Registration;
import com.project.ohl.application.port.in.AccountService;
import com.project.ohl.adapter.security.LoginThrottle;
import jakarta.servlet.http.HttpServletRequest;
import com.project.ohl.adapter.security.JwtTokens;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import java.util.UUID;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {
    private final AccountService accounts;
    private final AuthenticationManager authenticationManager;
    private final JwtTokens tokens;
    private final LoginThrottle throttle;

    public record Credentials(@NotBlank @Email @Size(max = 254) String email,
            @NotBlank String password) {
        @Override public String toString() { return "Credentials[email=" + email + "]"; }
    }
    @PostMapping("/login")
    public JwtTokens.AccessToken login(@Valid @RequestBody Credentials credentials, HttpServletRequest request) {
        String key = request.getRemoteAddr() + ":" + credentials.email().strip().toLowerCase(Locale.ROOT);
        throttle.check(key);
        var authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(credentials.email(), credentials.password()));
        throttle.succeeded(key);
        return tokens.issue((Account) authentication.getPrincipal());
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public JwtTokens.AccessToken register(@Valid @RequestBody Registration registration, HttpServletRequest request) {
        throttle.checkRegistration(request.getRemoteAddr());
        var account = accounts.register(registration);
        return tokens.issue(account);
    }

    @GetMapping("/me")
    public Account me(Authentication authentication) {
        return accounts.current(UUID.fromString(authentication.getName()));
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(Authentication authentication) {
        tokens.revoke(((JwtAuthenticationToken) authentication).getToken());
    }

    public record PasswordChange(@NotBlank String currentPassword, @NotBlank String newPassword) {
        @Override public String toString() { return "PasswordChange"; }
    }

    public record AccountDeletion(@NotBlank String currentPassword) {
        @Override public String toString() { return "AccountDeletion"; }
    }

    @PutMapping("/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(Authentication authentication, @Valid @RequestBody PasswordChange change,
            HttpServletRequest request) {
        String key = request.getRemoteAddr() + ":account:" + authentication.getName();
        throttle.check(key);
        accounts.updatePassword(UUID.fromString(authentication.getName()), change.currentPassword(), change.newPassword());
        throttle.succeeded(key);
        tokens.revoke(((JwtAuthenticationToken) authentication).getToken());
    }

    @DeleteMapping("/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAccount(Authentication authentication, @Valid @RequestBody AccountDeletion deletion,
            HttpServletRequest request) {
        String key = request.getRemoteAddr() + ":account:" + authentication.getName();
        throttle.check(key);
        accounts.deleteAccount(UUID.fromString(authentication.getName()), deletion.currentPassword());
        throttle.succeeded(key);
        tokens.revoke(((JwtAuthenticationToken) authentication).getToken());
    }
}
