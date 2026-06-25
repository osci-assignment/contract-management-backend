package com.osci.contractmanagement.application.service.user;

import com.osci.contractmanagement.application.dto.request.auth.LoginUserRequestDto;
import com.osci.contractmanagement.application.dto.response.auth.TokenResponseDto;
import com.osci.contractmanagement.application.exceptions.BusinessException;
import com.osci.contractmanagement.application.exceptions.BusinessExceptionType;
import com.osci.contractmanagement.application.provider.TokenProvider;
import com.osci.contractmanagement.application.provider.TokenType;
import com.osci.contractmanagement.domain.model.user.User;
import io.jsonwebtoken.Claims;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final UserCommandService userCommandService;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;

    public AuthService(UserCommandService userCommandService, PasswordEncoder passwordEncoder, TokenProvider tokenProvider) {
        this.userCommandService = userCommandService;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }


    @Transactional(readOnly = true)
    public TokenResponseDto login(LoginUserRequestDto request) {
        final User user = userCommandService.getActiveUserFromEmail(request.getEmail());

        if(!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(BusinessExceptionType.PASSWORD_NOT_MATCH);
        }

        String accessToken = tokenProvider.createToken(user.getId(), user.getRole(), TokenType.ACCESS);
        String refreshToken = tokenProvider.createToken(user.getId(), user.getRole(), TokenType.REFRESH);

        return TokenResponseDto.of(accessToken, refreshToken);
    }

    @Transactional(readOnly = true)
    public TokenResponseDto refresh(String refreshToken) {
        if (!tokenProvider.isValidToken(refreshToken)) {
            throw new BusinessException(BusinessExceptionType.INVALID_TOKEN);
        }

        Claims claims = tokenProvider.parseClaims(refreshToken);

        if (!TokenType.REFRESH.name().equals(claims.get("type", String.class))) {
            throw new BusinessException(BusinessExceptionType.INVALID_TOKEN);
        }

        Long userId = claims.get("userId", Long.class);
        User user = userCommandService.getUserFromId(userId);

        String newAccessToken = tokenProvider.createToken(user.getId(), user.getRole(), TokenType.ACCESS);
        String newRefreshToken = tokenProvider.createToken(user.getId(), user.getRole(), TokenType.REFRESH);

        return TokenResponseDto.of(newAccessToken, newRefreshToken);
    }
}