package com.penfit.penfit.global.security;

import com.penfit.penfit.global.config.JwtProperties;
import com.penfit.penfit.global.error.BusinessException;
import com.penfit.penfit.global.error.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtProviderTest {

    private static final String SECRET = "penfit-test-secret-key-with-more-than-32-bytes";

    private JwtProvider provider(long accessExpiration, long refreshExpiration) {
        return new JwtProvider(new JwtProperties(SECRET, accessExpiration, refreshExpiration));
    }

    @Test
    @DisplayName("32바이트 미만 시크릿은 기동 시점에 거부한다")
    void rejectsShortSecret() {
        assertThatThrownBy(() -> new JwtProvider(new JwtProperties("short", 1000, 1000)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("32바이트");
    }

    @Test
    @DisplayName("Access Token 에서 사용자 식별자를 복원한다")
    void parsesAccessToken() {
        JwtProvider provider = provider(3600000, 604800000);
        String token = provider.createAccessToken(42L);

        assertThat(provider.parseAccessToken(token)).isEqualTo(42L);
    }

    @Test
    @DisplayName("Refresh Token 에서 사용자 식별자를 복원한다")
    void parsesRefreshToken() {
        JwtProvider provider = provider(3600000, 604800000);
        String token = provider.createRefreshToken(42L);

        assertThat(provider.parseRefreshToken(token)).isEqualTo(42L);
    }

    @Test
    @DisplayName("Refresh Token 을 Access Token 자리에 쓰면 거부한다")
    void rejectsTokenTypeMismatch() {
        JwtProvider provider = provider(3600000, 604800000);
        String refreshToken = provider.createRefreshToken(42L);

        assertThatThrownBy(() -> provider.parseAccessToken(refreshToken))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_ACCESS_TOKEN);
    }

    @Test
    @DisplayName("만료된 토큰은 만료 코드로 구분한다")
    void distinguishesExpiredToken() {
        JwtProvider provider = provider(-1000, -1000);
        String token = provider.createAccessToken(42L);

        assertThatThrownBy(() -> provider.parseAccessToken(token))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.EXPIRED_ACCESS_TOKEN);
    }

    @Test
    @DisplayName("다른 키로 서명된 토큰은 거부한다")
    void rejectsTokenSignedWithOtherKey() {
        JwtProvider mine = provider(3600000, 604800000);
        JwtProvider other = new JwtProvider(
                new JwtProperties("another-secret-key-with-more-than-32-bytes-here", 3600000, 604800000));
        String forged = other.createAccessToken(42L);

        assertThatThrownBy(() -> mine.parseAccessToken(forged))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_ACCESS_TOKEN);
    }

    @Test
    @DisplayName("변조된 토큰은 거부한다")
    void rejectsTamperedToken() {
        JwtProvider provider = provider(3600000, 604800000);
        String token = provider.createAccessToken(42L);
        String tampered = token.substring(0, token.length() - 2) + "xy";

        assertThatThrownBy(() -> provider.parseAccessToken(tampered))
                .isInstanceOf(BusinessException.class);
    }
}
