package com.project.ohl.adapter.security;

import com.project.ohl.application.port.in.AccountService;
import com.project.ohl.domain.exception.AuthenticationFailedException;
import jakarta.servlet.DispatcherType;
import java.util.UUID;
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
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;

@Configuration(proxyBeanMethods = false)
@EnableMethodSecurity
public class SecurityConfig {
    @Bean
    AuthenticationManager authenticationManager(AccountService accounts) {
        return authentication -> {
            try {
                var account = accounts.authenticate(authentication.getName(), (String) authentication.getCredentials());
                return UsernamePasswordAuthenticationToken.authenticated(account, null,
                        AuthorityUtils.createAuthorityList("ROLE_" + account.role().name()));
            } catch (AuthenticationFailedException exception) {
                throw new BadCredentialsException("Invalid email or password.");
            }
        };
    }

    @Bean
    SecurityFilterChain security(HttpSecurity http, JwtTokens tokens, AccountService accounts) {
        return http.csrf(AbstractHttpConfigurer::disable)
                .requestCache(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable).httpBasic(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .sessionManagement(value -> value.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .oauth2ResourceServer(value -> value.jwt(jwt -> jwt.decoder(tokens::decode)
                        .jwtAuthenticationConverter(token -> {
                            String subject = token.getSubject();
                            if (subject == null) throw new InvalidBearerTokenException("Missing account");
                            try {
                                var account = accounts.current(UUID.fromString(subject));
                                return new JwtAuthenticationToken(token,
                                        AuthorityUtils.createAuthorityList("ROLE_" + account.role().name()), account.id().toString());
                            } catch (AuthenticationFailedException | IllegalArgumentException exception) {
                                throw new InvalidBearerTokenException("Invalid account", exception);
                            }
                        })))
                .authorizeHttpRequests(value -> value
                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/login", "/api/auth/register").permitAll()
                        .requestMatchers("/api/auth/**", "/api/categories/favorites/**").authenticated()
                        .requestMatchers("/api/users/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/media/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/counties", "/api/cities", "/api/schools/search", "/openapi.json").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/**").authenticated()
                        .anyRequest().hasRole("ADMIN"))
                .exceptionHandling(value -> value
                        .authenticationEntryPoint((request, response, exception) -> response.sendError(401))
                        .accessDeniedHandler((request, response, exception) -> response.sendError(403)))
                .headers(value -> value.cacheControl(Customizer.withDefaults()))
                .build();
    }
}
