package com.sparta.contractmanagement.application.service.user;

import com.sparta.contractmanagement.application.dto.request.auth.LoginUserDto;
import com.sparta.contractmanagement.application.dto.response.auth.TokenResponseDto;
import com.sparta.contractmanagement.application.exceptions.BusinessException;
import com.sparta.contractmanagement.application.exceptions.BusinessExceptionType;
import com.sparta.contractmanagement.application.provider.TokenProvider;
import com.sparta.contractmanagement.application.provider.TokenType;
import com.sparta.contractmanagement.domain.model.User;
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
    public TokenResponseDto login(LoginUserDto request) {
        final User user = userCommandService.getUserFromEmail(request.getEmail());

        if(!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(BusinessExceptionType.PASSWORD_NOT_MATCH);
        }

        String accessToken = tokenProvider.createToken(user.getId(), user.getRole(), TokenType.ACCESS);
        String refreshToken = tokenProvider.createToken(user.getId(), user.getRole(), TokenType.REFRESH);

        return TokenResponseDto.of(accessToken, refreshToken);
    }
}
