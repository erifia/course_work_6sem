package com.example.coursework6sem.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@Configuration
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final String googleClientId;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            @Value("${spring.security.oauth2.client.registration.google.client-id:}") String googleClientId
    ) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.googleClientId = googleClientId;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // ВРЕМЕННО: полностью открываем все API, чтобы стабильнее отладить
                // регистрацию/авторизацию и фронтенд. Потом можно вернуть RBAC.
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                )
                ;

        if (googleClientId != null && !googleClientId.isBlank()) {
            http.oauth2Login(Customizer.withDefaults());
        }

        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}

