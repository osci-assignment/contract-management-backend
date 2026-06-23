package com.sparta.contractmanagement.infrastructure.auth;

import com.sparta.contractmanagement.application.provider.TokenProvider;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticateFilter extends OncePerRequestFilter {
    private final TokenProvider tokenprovider;

    private static final String AUTHORIZATION_HEADER_KEY = "Authorization";
    private static final String TOKEN_PREFIX = "Bearer";

    public JwtAuthenticateFilter(TokenProvider tokenprovider) {
        this.tokenprovider = tokenprovider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authorizationHeader = request.getHeader(AUTHORIZATION_HEADER_KEY);

        if (authorizationHeader == null || !authorizationHeader.startsWith(TOKEN_PREFIX + " ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authorizationHeader.substring(7);

        try {
            Claims claims = tokenprovider.parseClaims(token);

            String username = claims.getSubject();
            String role = claims.get("role", String.class);

            User principal = new User(
                    username,
                    "",
                    List.of(() -> role)
            );

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            principal,
                            null,
                            principal.getAuthorities()
                    );

            authentication.setDetails(
                    new WebAuthenticationDetailsSource()
                            .buildDetails(request)
            );

            SecurityContextHolder.getContext()
                    .setAuthentication(authentication);

        } catch (Exception e) {
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}
