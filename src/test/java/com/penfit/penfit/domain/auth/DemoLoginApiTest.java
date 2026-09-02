package com.penfit.penfit.domain.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.penfit.penfit.domain.user.entity.User;
import com.penfit.penfit.domain.user.repository.UserRepository;
import com.penfit.penfit.global.client.kakao.KakaoOAuthClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
class DemoLoginApiTest {

    private static final String DEMO_URL = "/api/v1/auth/demo-login";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @MockitoBean
    private KakaoOAuthClient kakaoOAuthClient;

    @Test
    @DisplayName("카카오 없이 토큰을 발급하고 그 토큰으로 API 를 쓸 수 있다")
    void issuesUsableToken() throws Exception {
        String body = mockMvc.perform(post(DEMO_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").exists())
                .andExpect(jsonPath("$.data.newUser").value(true))
                .andExpect(header().exists(HttpHeaders.SET_COOKIE))
                .andReturn().getResponse().getContentAsString();

        String token = objectMapper.readTree(body).path("data").path("accessToken").asText();

        mockMvc.perform(get("/api/v1/users/me").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("누를 때마다 서로 다른 계정이 만들어진다")
    void createsSeparateAccounts() throws Exception {
        long first = loginAndGetUserId();
        long second = loginAndGetUserId();

        assertThat(first).isNotEqualTo(second);
    }

    @Test
    @DisplayName("발급된 계정은 데모로 표시되고 이메일이 비어 있다")
    void marksAccountAsDemo() throws Exception {
        User user = userRepository.findById(loginAndGetUserId()).orElseThrow();

        assertThat(user.isDemo()).isTrue();
        assertThat(user.hasEmail()).isFalse();
        assertThat(user.isEmailConsent()).isFalse();
        assertThat(user.getKakaoId()).startsWith("demo-");
    }

    @Test
    @DisplayName("데모 계정도 리허설을 처음부터 진행할 수 있다")
    void startsFromEmptyState() throws Exception {
        String body = mockMvc.perform(post(DEMO_URL))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String token = objectMapper.readTree(body).path("data").path("accessToken").asText();

        mockMvc.perform(get("/api/v1/users/me/financial-profile")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("FP4041"));
    }

    private long loginAndGetUserId() throws Exception {
        String body = mockMvc.perform(post(DEMO_URL))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).path("data").path("userId").asLong();
    }

    @SpringBootTest
    @AutoConfigureMockMvc
    @TestPropertySource(properties = "penfit.demo.enabled=false")
    static class DisabledTest {

        @Autowired
        private MockMvc mockMvc;

        @MockitoBean
        private KakaoOAuthClient kakaoOAuthClient;

        @Test
        @DisplayName("기능이 꺼져 있으면 찾을 수 없다고 응답한다")
        void rejectsWhenDisabled() throws Exception {
            mockMvc.perform(post(DEMO_URL))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("CM4041"));
        }
    }
}
