package com.penfit.penfit.domain.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.penfit.penfit.global.client.kakao.KakaoOAuthClient;
import com.penfit.penfit.global.client.kakao.KakaoUserInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
class UserApiTest {

    private static final String ME_URL = "/api/v1/users/me";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private KakaoOAuthClient kakaoOAuthClient;

    private String accessToken;

    @BeforeEach
    void setUp() throws Exception {
        given(kakaoOAuthClient.fetchUserInfo(anyString()))
                .willReturn(new KakaoUserInfo("kakao-1", "이재원"));

        String body = mockMvc.perform(post("/api/v1/auth/kakao/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"authorization-code\"}"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        accessToken = objectMapper.readTree(body).path("data").path("accessToken").asText();
    }

    @Test
    @DisplayName("내 회원정보를 조회한다")
    void getMyInfo() throws Exception {
        mockMvc.perform(get(ME_URL).header(HttpHeaders.AUTHORIZATION, bearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nickname").value("이재원"))
                .andExpect(jsonPath("$.data.emailConsent").value(false))
                .andExpect(jsonPath("$.data.loginProvider").value("KAKAO"));
    }

    @Test
    @DisplayName("닉네임을 수정한다")
    void updateNickname() throws Exception {
        mockMvc.perform(patch(ME_URL + "/nickname")
                        .header(HttpHeaders.AUTHORIZATION, bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nickname\":\"다운\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nickname").value("다운"));
    }

    @Test
    @DisplayName("빈 닉네임은 400 으로 막는다")
    void rejectsBlankNickname() throws Exception {
        mockMvc.perform(patch(ME_URL + "/nickname")
                        .header(HttpHeaders.AUTHORIZATION, bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nickname\":\"  \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("CM4001"));
    }

    @Test
    @DisplayName("이메일을 등록해도 수신 동의는 켜지지 않는다")
    void registeringEmailKeepsConsentOff() throws Exception {
        mockMvc.perform(put(ME_URL + "/email")
                        .header(HttpHeaders.AUTHORIZATION, bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"jaewon@example.com\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("jaewon@example.com"))
                .andExpect(jsonPath("$.data.emailConsent").value(false));
    }

    @Test
    @DisplayName("이메일 형식이 아니면 400 으로 막는다")
    void rejectsInvalidEmail() throws Exception {
        mockMvc.perform(put(ME_URL + "/email")
                        .header(HttpHeaders.AUTHORIZATION, bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"not-an-email\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("CM4001"));
    }

    @Test
    @DisplayName("이메일이 없으면 수신 동의를 켤 수 없다")
    void cannotEnableConsentWithoutEmail() throws Exception {
        mockMvc.perform(patch(ME_URL + "/email-consent")
                        .header(HttpHeaders.AUTHORIZATION, bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"emailConsent\":true}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("US4091"));
    }

    @Test
    @DisplayName("이메일을 등록한 뒤에는 수신 동의를 켤 수 있다")
    void enablesConsentAfterEmailRegistered() throws Exception {
        registerEmail();

        mockMvc.perform(patch(ME_URL + "/email-consent")
                        .header(HttpHeaders.AUTHORIZATION, bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"emailConsent\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.emailConsent").value(true));
    }

    @Test
    @DisplayName("이메일을 삭제하면 수신 동의도 함께 꺼진다")
    void deletingEmailDisablesConsent() throws Exception {
        registerEmail();
        mockMvc.perform(patch(ME_URL + "/email-consent")
                        .header(HttpHeaders.AUTHORIZATION, bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"emailConsent\":true}"))
                .andExpect(status().isOk());

        mockMvc.perform(delete(ME_URL + "/email").header(HttpHeaders.AUTHORIZATION, bearer()))
                .andExpect(status().isOk());

        mockMvc.perform(get(ME_URL).header(HttpHeaders.AUTHORIZATION, bearer()))
                .andExpect(jsonPath("$.data.email").doesNotExist())
                .andExpect(jsonPath("$.data.emailConsent").value(false));
    }

    @Test
    @DisplayName("이미 이메일이 없어도 삭제는 성공한다")
    void deletingEmailIsIdempotent() throws Exception {
        mockMvc.perform(delete(ME_URL + "/email").header(HttpHeaders.AUTHORIZATION, bearer()))
                .andExpect(status().isOk());
        mockMvc.perform(delete(ME_URL + "/email").header(HttpHeaders.AUTHORIZATION, bearer()))
                .andExpect(status().isOk());
    }

    private void registerEmail() throws Exception {
        mockMvc.perform(put(ME_URL + "/email")
                        .header(HttpHeaders.AUTHORIZATION, bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"jaewon@example.com\"}"))
                .andExpect(status().isOk());
    }

    private String bearer() {
        return "Bearer " + accessToken;
    }
}
