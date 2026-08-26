package com.penfit.penfit.global.client.kakao;

import com.penfit.penfit.global.client.kakao.dto.KakaoTokenResponse;
import com.penfit.penfit.global.client.kakao.dto.KakaoUserResponse;
import com.penfit.penfit.global.config.KakaoProperties;
import com.penfit.penfit.global.error.BusinessException;
import com.penfit.penfit.global.error.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
public class KakaoOAuthClient {

    private static final String DEFAULT_NICKNAME = "회원";

    private final RestClient restClient;
    private final KakaoProperties properties;

    public KakaoOAuthClient(KakaoProperties properties) {
        this.properties = properties;

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout((int) properties.connectTimeout().toMillis());
        factory.setReadTimeout((int) properties.readTimeout().toMillis());

        this.restClient = RestClient.builder()
                .requestFactory(factory)
                .build();
    }

    public KakaoUserInfo fetchUserInfo(String authorizationCode) {
        String kakaoAccessToken = requestAccessToken(authorizationCode);
        KakaoUserResponse user = requestUserInfo(kakaoAccessToken);

        if (user == null || user.id() == null) {
            throw new BusinessException(ErrorCode.KAKAO_AUTH_FAILED);
        }

        String nickname = user.nicknameOrNull();
        return new KakaoUserInfo(
                String.valueOf(user.id()),
                (nickname == null || nickname.isBlank()) ? DEFAULT_NICKNAME : nickname);
    }

    private String requestAccessToken(String authorizationCode) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "authorization_code");
        form.add("client_id", properties.clientId());
        form.add("client_secret", properties.clientSecret());
        form.add("redirect_uri", properties.redirectUri());
        form.add("code", authorizationCode);

        KakaoTokenResponse response = execute(() -> restClient.post()
                .uri(properties.tokenUri())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .onStatus(status -> status.is4xxClientError(), (request, clientResponse) -> {
                    log.warn("카카오 토큰 발급 실패 status={}", clientResponse.getStatusCode());
                    throw new BusinessException(ErrorCode.KAKAO_AUTH_FAILED);
                })
                .onStatus(status -> status.is5xxServerError(), (request, clientResponse) -> {
                    log.error("카카오 인증 서버 오류 status={}", clientResponse.getStatusCode());
                    throw new BusinessException(ErrorCode.KAKAO_SERVER_ERROR);
                })
                .body(KakaoTokenResponse.class));

        if (response == null || response.accessToken() == null) {
            throw new BusinessException(ErrorCode.KAKAO_AUTH_FAILED);
        }
        return response.accessToken();
    }

    private KakaoUserResponse requestUserInfo(String kakaoAccessToken) {
        return execute(() -> restClient.get()
                .uri(properties.userInfoUri())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + kakaoAccessToken)
                .retrieve()
                .onStatus(status -> status.is4xxClientError(), (request, clientResponse) -> {
                    log.warn("카카오 사용자 조회 실패 status={}", clientResponse.getStatusCode());
                    throw new BusinessException(ErrorCode.KAKAO_AUTH_FAILED);
                })
                .onStatus(status -> status.is5xxServerError(), (request, clientResponse) -> {
                    log.error("카카오 API 서버 오류 status={}", clientResponse.getStatusCode());
                    throw new BusinessException(ErrorCode.KAKAO_SERVER_ERROR);
                })
                .body(KakaoUserResponse.class));
    }

    private <T> T execute(java.util.function.Supplier<T> call) {
        try {
            return call.get();
        } catch (BusinessException e) {
            throw e;
        } catch (ResourceAccessException e) {
            log.error("카카오 서버 통신 실패", e);
            throw new BusinessException(ErrorCode.KAKAO_SERVER_ERROR, e);
        }
    }
}
