package com.penfit.penfit.domain.pensionsetup;

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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
class PensionAccountTypeApiTest {

    private static final String URL = "/api/v1/pension-setups/account-types";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private KakaoOAuthClient kakaoOAuthClient;

    private String accessToken;

    @BeforeEach
    void setUp() throws Exception {
        given(kakaoOAuthClient.fetchUserInfo(anyString(), any()))
                .willReturn(new KakaoUserInfo("kakao-1", "이재원"));

        String body = mockMvc.perform(post("/api/v1/auth/kakao/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"authorization-code\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        accessToken = objectMapper.readTree(body).path("data").path("accessToken").asText();
    }

    @Test
    @DisplayName("계좌 3종을 화면 노출 순서대로 반환한다")
    void returnsThreeAccountTypesInOrder() throws Exception {
        mockMvc.perform(get(URL).header(HttpHeaders.AUTHORIZATION, bearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(3))
                .andExpect(jsonPath("$.data[0].code").value("PENSION_SAVINGS_FUND"))
                .andExpect(jsonPath("$.data[1].code").value("INDIVIDUAL_IRP"))
                .andExpect(jsonPath("$.data[2].code").value("PENSION_SAVINGS_INSURANCE"));
    }

    @Test
    @DisplayName("계좌 선택 화면에 필요한 제목·설명·태그를 담는다")
    void containsSelectionScreenFields() throws Exception {
        mockMvc.perform(get(URL).header(HttpHeaders.AUTHORIZATION, bearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("연금저축펀드"))
                .andExpect(jsonPath("$.data[0].title").isNotEmpty())
                .andExpect(jsonPath("$.data[0].description").isNotEmpty())
                .andExpect(jsonPath("$.data[0].tags.length()").value(4));
    }

    @Test
    @DisplayName("비교 화면에 필요한 네 가지 기준을 모두 담는다")
    void containsComparisonFields() throws Exception {
        for (int index = 0; index < 3; index++) {
            mockMvc.perform(get(URL).header(HttpHeaders.AUTHORIZATION, bearer()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data[%d].comparison.investmentStyle".formatted(index)).isNotEmpty())
                    .andExpect(jsonPath("$.data[%d].comparison.taxBenefit".formatted(index)).isNotEmpty())
                    .andExpect(jsonPath("$.data[%d].comparison.keyFeature".formatted(index)).isNotEmpty())
                    .andExpect(jsonPath("$.data[%d].comparison.recommendedFor".formatted(index)).isNotEmpty());
        }
    }

    @Test
    @DisplayName("토큰 없이 호출하면 401 을 반환한다")
    void rejectsWithoutToken() throws Exception {
        mockMvc.perform(get(URL))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("CM4011"));
    }

    private String bearer() {
        return "Bearer " + accessToken;
    }
}
