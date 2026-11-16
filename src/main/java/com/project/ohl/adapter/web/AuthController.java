package com.project.ohl.adapter.web;

import com.project.ohl.application.model.Account;
import com.project.ohl.application.model.Registration;
import com.project.ohl.application.port.in.AccountService;
import com.project.ohl.adapter.security.LoginThrottle;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
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
    private final SessionAuthenticationStrategy sessions;
    private final SecurityContextRepository contexts;
    private final LoginThrottle throttle;

    public record Credentials(@NotBlank @Email @Size(max = 254) String email,
            @NotBlank @Size(max = 72) String password) {
        @Override public String toString() { return "Credentials[email=" + email + "]"; }
    }
    public record CsrfResponse(String token) {}

    @GetMapping("/csrf")
    public CsrfResponse csrf(CsrfToken token) { return new CsrfResponse(token.getToken()); }

    @PostMapping("/login")
    public Account login(@Valid @RequestBody Credentials credentials, HttpServletRequest request,
            HttpServletResponse response) {
        String key = request.getRemoteAddr() + ":" + credentials.email().strip().toLowerCase(Locale.ROOT);
        throttle.check(key);
        var authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(credentials.email(), credentials.password()));
        establish(authentication, request, response);
        throttle.succeeded(key);
        return (Account) authentication.getPrincipal();
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public Account register(@Valid @RequestBody Registration registration, HttpServletRequest request,
            HttpServletResponse response) {
        throttle.checkRegistration(request.getRemoteAddr());
        var account = accounts.register(registration);
        establish(UsernamePasswordAuthenticationToken.authenticated(account, null,
                AuthorityUtils.createAuthorityList("ROLE_" + account.authority().name())), request, response);
        return account;
    }

    @GetMapping("/me")
    public Account me(Authentication authentication) {
        return accounts.current(((Account) authentication.getPrincipal()).id());
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(Authentication authentication, HttpServletRequest request, HttpServletResponse response) {
        new SecurityContextLogoutHandler().logout(request, response, authentication);
    }

    private void establish(Authentication authentication, HttpServletRequest request, HttpServletResponse response) {
        sessions.onAuthentication(authentication, request, response);
        var context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        contexts.saveContext(context, request, response);
    }
}
