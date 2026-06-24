package com.osci.contractmanagement.application.provider;

import com.osci.contractmanagement.domain.model.user.UserRole;
import io.jsonwebtoken.Claims;

public interface TokenProvider {
    String createToken(Long userId, UserRole role, TokenType type);
    boolean isValidToken(String accessToken);
    Claims parseClaims(String accessToken);
}
