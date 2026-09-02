package com.penfit.penfit.global.client.kakao;

import com.penfit.penfit.global.config.KakaoProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KakaoRedirectUriResolverTest {

    private static final String DEPLOYED = "https://www.penfit.store/oauth/kakao/callback";
    private static final String LOCAL = "http://localhost:5173/oauth/kakao/callback";

    private final KakaoRedirectUriResolver resolver = new KakaoRedirectUriResolver(properties(DEPLOYED, LOCAL));

    @Test
    @DisplayName("Origin 과 일치하는 리다이렉트 URI 를 고른다")
    void resolveMatchingOrigin() {
        assertThat(resolver.resolve("http://localhost:5173")).isEqualTo(LOCAL);
        assertThat(resolver.resolve("https://www.penfit.store")).isEqualTo(DEPLOYED);
    }

    @Test
    @DisplayName("Origin 이 없으면 첫 번째 값을 쓴다")
    void resolveWithoutOrigin() {
        assertThat(resolver.resolve(null)).isEqualTo(DEPLOYED);
        assertThat(resolver.resolve("  ")).isEqualTo(DEPLOYED);
    }

    @Test
    @DisplayName("허용 목록에 없는 Origin 은 무시하고 첫 번째 값을 쓴다")
    void ignoreUnknownOrigin() {
        assertThat(resolver.resolve("https://attacker.example.com")).isEqualTo(DEPLOYED);
    }

    @Test
    @DisplayName("허용 Origin 을 접두사로 흉내 낸 주소는 통하지 않는다")
    void rejectPrefixLookalike() {
        assertThat(resolver.resolve("https://www.penfit.store.attacker.com")).isEqualTo(DEPLOYED);
    }

    @Test
    @DisplayName("리다이렉트 URI 설정이 비어 있으면 기동에 실패한다")
    void requireConfiguredUris() {
        assertThatThrownBy(() -> new KakaoRedirectUriResolver(properties()))
                .isInstanceOf(IllegalStateException.class);
    }

    private KakaoProperties properties(String... redirectUris) {
        return new KakaoProperties("client-id", "client-secret", List.of(redirectUris),
                "https://kauth.kakao.com/oauth/token", "https://kapi.kakao.com/v2/user/me",
                Duration.ofSeconds(3), Duration.ofSeconds(5));
    }
}
