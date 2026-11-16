package com.project.ohl.adapter.security;

import com.project.ohl.application.port.in.AccountService;
import com.project.ohl.domain.exception.AuthenticationFailedException;
import jakarta.servlet.DispatcherType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfAuthenticationStrategy;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.security.web.authentication.session.ChangeSessionIdAuthenticationStrategy;
import org.springframework.security.web.authentication.session.CompositeSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import java.util.List;

@Configuration(proxyBeanMethods = false)
@EnableMethodSecurity
public class SecurityConfig {
    @Bean
    SecurityContextRepository securityContexts() { return new HttpSessionSecurityContextRepository(); }

    @Bean
    CsrfTokenRepository csrfTokens() { return new HttpSessionCsrfTokenRepository(); }

    @Bean
    SessionAuthenticationStrategy sessionAuthentication(CsrfTokenRepository csrf) {
        return new CompositeSessionAuthenticationStrategy(List.of(new ChangeSessionIdAuthenticationStrategy(),
                new CsrfAuthenticationStrategy(csrf)));
    }

    @Bean
    AuthenticationManager authenticationManager(AccountService accounts) {
        return authentication -> {
            try {
                var account = accounts.authenticate(authentication.getName(), (String) authentication.getCredentials());
                return UsernamePasswordAuthenticationToken.authenticated(account, null,
                        AuthorityUtils.createAuthorityList("ROLE_" + account.authority().name()));
            } catch (AuthenticationFailedException exception) {
                throw new BadCredentialsException("Invalid email or password.");
            }
        };
    }

    @Bean
    SecurityFilterChain security(HttpSecurity http, SecurityContextRepository contexts,
            CsrfTokenRepository csrf) throws Exception {
        return http.securityContext(value -> value.securityContextRepository(contexts))
                .csrf(value -> value.csrfTokenRepository(csrf))
                .requestCache(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable).httpBasic(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .sessionManagement(Customizer.withDefaults())
                .authorizeHttpRequests(value -> value
                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                        .requestMatchers("/api/auth/csrf", "/api/auth/login", "/api/auth/register").permitAll()
                        .requestMatchers("/api/auth/**", "/api/categories/favorites/**").authenticated()
                        .requestMatchers("/api/users/**", "/api/roles/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/**", "/openapi.json").permitAll()
                        .anyRequest().hasRole("ADMIN"))
                .exceptionHandling(value -> value
                        .authenticationEntryPoint((request, response, exception) -> response.sendError(401))
                        .accessDeniedHandler((request, response, exception) -> response.sendError(403)))
                .headers(value -> value.cacheControl(Customizer.withDefaults()))
                .build();
    }
}
