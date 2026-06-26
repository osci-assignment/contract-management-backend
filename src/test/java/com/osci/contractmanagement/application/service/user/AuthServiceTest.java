package com.osci.contractmanagement.application.service.user;

import com.osci.contractmanagement.application.dto.request.auth.LoginUserRequestDto;
import com.osci.contractmanagement.application.dto.response.auth.TokenResponseDto;
import com.osci.contractmanagement.application.exceptions.BusinessException;
import com.osci.contractmanagement.application.exceptions.BusinessExceptionType;
import com.osci.contractmanagement.application.provider.TokenProvider;
import com.osci.contractmanagement.application.provider.TokenType;
import com.osci.contractmanagement.domain.model.user.User;
import com.osci.contractmanagement.domain.model.user.UserRole;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserCommandService userCommandService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private TokenProvider tokenProvider;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userCommandService, passwordEncoder, tokenProvider);
    }

    @Nested
    @DisplayName("login")
    class Login {

        @Test
        @DisplayName("이메일/비밀번호가 일치하면 access/refresh 토큰을 발급한다")
        void login_success() {
            User user = User.create("worker@osci.com", "encodedPw");
            user.approve();
            LoginUserRequestDto request = new LoginUserRequestDto("worker@osci.com", "rawPw");

            when(userCommandService.getActiveUserFromEmail("worker@osci.com")).thenReturn(user);
            when(passwordEncoder.matches("rawPw", "encodedPw")).thenReturn(true);
            when(tokenProvider.createToken(any(), any(), eq(TokenType.ACCESS))).thenReturn("access-token");
            when(tokenProvider.createToken(any(), any(), eq(TokenType.REFRESH))).thenReturn("refresh-token");

            TokenResponseDto response = authService.login(request);

            assertThat(response.getAccessToken()).isEqualTo("access-token");
            assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
        }

        @Test
        @DisplayName("비밀번호가 일치하지 않으면 PASSWORD_NOT_MATCH 예외를 던진다")
        void login_wrongPassword_throws() {
            User user = User.create("worker@osci.com", "encodedPw");
            LoginUserRequestDto request = new LoginUserRequestDto("worker@osci.com", "wrongPw");

            when(userCommandService.getActiveUserFromEmail("worker@osci.com")).thenReturn(user);
            when(passwordEncoder.matches("wrongPw", "encodedPw")).thenReturn(false);

            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getType())
                    .isEqualTo(BusinessExceptionType.PASSWORD_NOT_MATCH);
        }
    }

    @Nested
    @DisplayName("refresh")
    class Refresh {

        @Test
        @DisplayName("유효한 REFRESH 토큰이면 새 access/refresh 토큰을 발급한다")
        void refresh_success() {
            String refreshToken = "valid-refresh-token";
            Claims claims = mock(Claims.class);
            User user = User.create("worker@osci.com", "encodedPw");

            when(tokenProvider.isValidToken(refreshToken)).thenReturn(true);
            when(tokenProvider.parseClaims(refreshToken)).thenReturn(claims);
            when(claims.get("type", String.class)).thenReturn(TokenType.REFRESH.name());
            when(claims.get("userId", Long.class)).thenReturn(1L);
            when(userCommandService.getUserFromId(1L)).thenReturn(user);
            when(tokenProvider.createToken(any(), any(), eq(TokenType.ACCESS))).thenReturn("new-access-token");
            when(tokenProvider.createToken(any(), any(), eq(TokenType.REFRESH))).thenReturn("new-refresh-token");

            TokenResponseDto response = authService.refresh(refreshToken);

            assertThat(response.getAccessToken()).isEqualTo("new-access-token");
            assertThat(response.getRefreshToken()).isEqualTo("new-refresh-token");
        }

        @Test
        @DisplayName("서명/만료 검증에 실패하면 INVALID_TOKEN 예외를 던진다")
        void refresh_invalidSignature_throws() {
            when(tokenProvider.isValidToken("broken-token")).thenReturn(false);

            assertThatThrownBy(() -> authService.refresh("broken-token"))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getType())
                    .isEqualTo(BusinessExceptionType.INVALID_TOKEN);

            verify(tokenProvider, never()).parseClaims(any());
        }

        @Test
        @DisplayName("type 클레임이 ACCESS면(=accessToken을 잘못 넣은 경우) INVALID_TOKEN 예외를 던진다")
        void refresh_accessTokenMisused_throws() {
            String accessToken = "access-token-not-refresh";
            Claims claims = mock(Claims.class);

            when(tokenProvider.isValidToken(accessToken)).thenReturn(true);
            when(tokenProvider.parseClaims(accessToken)).thenReturn(claims);
            when(claims.get("type", String.class)).thenReturn(TokenType.ACCESS.name());

            assertThatThrownBy(() -> authService.refresh(accessToken))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getType())
                    .isEqualTo(BusinessExceptionType.INVALID_TOKEN);

            verify(userCommandService, never()).getUserFromId(any());
        }
    }
}
