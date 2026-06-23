package com.sparta.contractmanagement.application.provider;

import io.jsonwebtoken.Claims;

public interface TokenProvider {
    String createToken(Long userId, String role);
    boolean isValidToken(String accessToken);
    Claims parseClaims(String accessToken);
}
