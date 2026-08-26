package com.penfit.penfit.domain.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.penfit.penfit.global.client.kakao.KakaoOAuthClient;
import com.penfit.penfit.global.client.kakao.KakaoUserInfo;
import com.penfit.penfit.global.error.BusinessException;
import com.penfit.penfit.global.error.ErrorCode;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
class AuthApiTest {

    private static final String LOGIN_URL = "/api/v1/auth/kakao/login";
    private static final String REISSUE_URL = "/api/v1/auth/reissue";
    private static final String LOGOUT_URL = "/api/v1/auth/logout";
    private static final String LOGIN_BODY = "{\"code\":\"authorization-code\"}";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private KakaoOAuthClient kakaoOAuthClient;

    @BeforeEach
    void setUp() {
        given(kakaoOAuthClient.fetchUserInfo(anyString()))
                .willReturn(new KakaoUserInfo("kakao-1", "이재원"));
    }

    @Test
    @DisplayName("최초 로그인이면 회원을 생성하고 토큰과 쿠키를 함께 내려준다")
    void createsUserOnFirstLogin() throws Exception {
        MvcResult result = mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(LOGIN_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("CM2001"))
                .andExpect(jsonPath("$.data.nickname").value("이재원"))
                .andExpect(jsonPath("$.data.newUser").value(true))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andReturn();

        Cookie cookie = result.getResponse().getCookie("refreshToken");
        assertThat(cookie).isNotNull();
        assertThat(cookie.isHttpOnly()).isTrue();
        assertThat(cookie.getValue()).isNotBlank();
    }

    @Test
    @DisplayName("같은 카카오 계정으로 다시 로그인하면 회원을 새로 만들지 않는다")
    void reusesUserOnSecondLogin() throws Exception {
        login();

        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(LOGIN_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.newUser").value(false));
    }

    @Test
    @DisplayName("인가 코드가 비어 있으면 400 으로 막는다")
    void rejectsBlankCode() throws Exception {
        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("CM4001"));
    }

    @Test
    @DisplayName("카카오 인증에 실패하면 401 AU4015 를 반환한다")
    void returnsUnauthorizedWhenKakaoRejects() throws Exception {
        willThrow(new BusinessException(ErrorCode.KAKAO_AUTH_FAILED))
                .given(kakaoOAuthClient).fetchUserInfo(anyString());

        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(LOGIN_BODY))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AU4015"));
    }

    @Test
    @DisplayName("토큰 없이 보호된 API 를 호출하면 401 CM4011 을 반환한다")
    void rejectsRequestWithoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("CM4011"));
    }

    @Test
    @DisplayName("잘못된 토큰으로 호출하면 401 AU4011 을 반환한다")
    void rejectsInvalidToken() throws Exception {
        mockMvc.perform(get("/api/v1/users/me").header("Authorization", "Bearer not-a-jwt"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AU4011"));
    }

    @Test
    @DisplayName("쿠키의 Refresh Token 으로 Access Token 을 재발급한다")
    void reissuesAccessToken() throws Exception {
        MvcResult loginResult = login();
        Cookie refreshCookie = loginResult.getResponse().getCookie("refreshToken");

        mockMvc.perform(post(REISSUE_URL).cookie(refreshCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty());
    }

    @Test
    @DisplayName("쿠키가 없으면 재발급을 거부한다")
    void rejectsReissueWithoutCookie() throws Exception {
        mockMvc.perform(post(REISSUE_URL))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AU4013"));
    }

    @Test
    @DisplayName("로그아웃하면 쿠키를 만료시키고 이후 재발급을 거부한다")
    void logoutRevokesRefreshToken() throws Exception {
        MvcResult loginResult = login();
        Cookie refreshCookie = loginResult.getResponse().getCookie("refreshToken");

        MvcResult logoutResult = mockMvc.perform(post(LOGOUT_URL).cookie(refreshCookie))
                .andExpect(status().isOk())
                .andReturn();

        Cookie expired = logoutResult.getResponse().getCookie("refreshToken");
        assertThat(expired).isNotNull();
        assertThat(expired.getMaxAge()).isZero();

        mockMvc.perform(post(REISSUE_URL).cookie(refreshCookie))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AU4013"));
    }

    private MvcResult login() throws Exception {
        return mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(LOGIN_BODY))
                .andExpect(status().isOk())
                .andReturn();
    }

    String accessTokenFrom(MvcResult result) throws Exception {
        JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
        return node.path("data").path("accessToken").asText();
    }
}
