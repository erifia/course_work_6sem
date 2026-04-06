package com.example.coursework6sem.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SecurityUtils {

    public Optional<Long> currentUserAccountId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return Optional.empty();
        }
        Object principal = auth.getPrincipal();
        if (principal instanceof Long l) {
            return Optional.of(l);
        }
        if (principal instanceof String s && !s.isBlank()) {
            return Optional.of(Long.parseLong(s));
        }
        return Optional.empty();
    }

    public Optional<String> currentRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return Optional.empty();
        }
        for (GrantedAuthority authority : auth.getAuthorities()) {
            String a = authority.getAuthority();
            // In JwtAuthenticationFilter we use: ROLE_<ROLE>
            if (a != null && a.startsWith("ROLE_")) {
                return Optional.of(a.substring("ROLE_".length()));
            }
        }
        return Optional.empty();
    }
}

