package com.example.coursework6sem.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring("Bearer ".length());
        try {
            Claims claims = jwtService.parseAccessToken(token);
            String role = jwtService.getRole(claims);
            long userAccountId = jwtService.getUserAccountId(claims);

            var authorities = role == null
                    ? Collections.<SimpleGrantedAuthority>emptyList()
                    : Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()));

            var authentication = new UsernamePasswordAuthenticationToken(
                    userAccountId,
                    null,
                    authorities
            );
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (Exception ignored) {
            // Непарсибельный/просроченный токен просто не аутентифицирует запрос.
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}

