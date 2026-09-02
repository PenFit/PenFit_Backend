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
class PensionSetupApiTest {

    private static final String SETUP_URL = "/api/v1/users/me/pension-setup";

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
    @DisplayName("설정 전에 조회하면 PS4041 로 안내한다")
    void returnsNotFoundBeforeSetup() throws Exception {
        mockMvc.perform(get(SETUP_URL).header(HttpHeaders.AUTHORIZATION, bearer()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PS4041"));
    }

    @Test
    @DisplayName("등록하면 30년 예상 연금자산을 함께 반환한다")
    void createsSetupWithPreview() throws Exception {
        mockMvc.perform(post(SETUP_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("PENSION_SAVINGS_FUND", 100_000)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("CM2011"))
                .andExpect(jsonPath("$.data.accountType.code").value("PENSION_SAVINGS_FUND"))
                .andExpect(jsonPath("$.data.accountType.displayName").value("연금저축펀드"))
                .andExpect(jsonPath("$.data.monthlyContribution").value(100_000))
                .andExpect(jsonPath("$.data.previewFutureAsset").value(83_225_864L))
                .andExpect(jsonPath("$.data.contributionYears").value(30));
    }

    @Test
    @DisplayName("차트용 구간별 예상금액을 10년부터 30년까지 5개 반환한다")
    void returnsGrowthMilestones() throws Exception {
        mockMvc.perform(post(SETUP_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("PENSION_SAVINGS_FUND", 100_000)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.growth.length()").value(5))
                .andExpect(jsonPath("$.data.growth[0].years").value(10))
                .andExpect(jsonPath("$.data.growth[4].years").value(30))
                .andExpect(jsonPath("$.data.growth[4].futureAsset").value(83_225_864L));
    }

    @Test
    @DisplayName("등록 후에는 조회된다")
    void readsAfterCreation() throws Exception {
        register();

        mockMvc.perform(get(SETUP_URL).header(HttpHeaders.AUTHORIZATION, bearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.monthlyContribution").value(100_000))
                .andExpect(jsonPath("$.data.previewFutureAsset").value(83_225_864L));
    }

    @Test
    @DisplayName("두 번째 등록은 PS4091 로 막는다")
    void rejectsSecondSetup() throws Exception {
        register();

        mockMvc.perform(post(SETUP_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("INDIVIDUAL_IRP", 200_000)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("PS4091"));
    }

    @Test
    @DisplayName("월 납입액이 5만원 미만이면 PS4001 로 막는다")
    void rejectsContributionBelowMinimum() throws Exception {
        mockMvc.perform(post(SETUP_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("PENSION_SAVINGS_FUND", 40_000)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("PS4001"));
    }

    @Test
    @DisplayName("하한인 5만원은 허용한다")
    void acceptsMinimumContribution() throws Exception {
        mockMvc.perform(post(SETUP_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("PENSION_SAVINGS_FUND", 50_000)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.monthlyContribution").value(50_000));
    }

    @Test
    @DisplayName("상한이 없어 큰 금액도 허용한다")
    void acceptsLargeContribution() throws Exception {
        mockMvc.perform(post(SETUP_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("PENSION_SAVINGS_FUND", 5_000_000)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.previewFutureAsset").value(4_161_293_177L));
    }

    @Test
    @DisplayName("허용되지 않은 계좌 코드는 사용 가능한 값을 알려준다")
    void rejectsUnknownAccountType() throws Exception {
        mockMvc.perform(post(SETUP_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("NOT_EXIST", 100_000)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("CM4001"))
                .andExpect(jsonPath("$.message").value(
                        org.hamcrest.Matchers.containsString("PENSION_SAVINGS_FUND")));
    }

    @Test
    @DisplayName("토큰 없이 호출하면 401 을 반환한다")
    void rejectsWithoutToken() throws Exception {
        mockMvc.perform(get(SETUP_URL))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("CM4011"));
    }

    private void register() throws Exception {
        mockMvc.perform(post(SETUP_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("PENSION_SAVINGS_FUND", 100_000)))
                .andExpect(status().isCreated());
    }

    private String body(String accountType, long monthlyContribution) {
        return """
                {"accountType": "%s", "monthlyContribution": %d}
                """.formatted(accountType, monthlyContribution);
    }

    private String bearer() {
        return "Bearer " + accessToken;
    }
}
