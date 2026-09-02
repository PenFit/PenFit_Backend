package com.penfit.penfit.domain.pensionplan;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.penfit.penfit.domain.passport.entity.PensionPassport;
import com.penfit.penfit.domain.passport.repository.PensionPassportRepository;
import com.penfit.penfit.global.client.ai.AiApi;
import com.penfit.penfit.global.client.ai.AiClient;
import com.penfit.penfit.global.client.ai.dto.PensionPlanGenerateResponse;
import com.penfit.penfit.global.client.kakao.KakaoOAuthClient;
import com.penfit.penfit.global.client.kakao.KakaoUserInfo;
import com.penfit.penfit.global.enums.MarketRiskLevel;
import com.penfit.penfit.global.enums.PassportTypeCode;
import com.penfit.penfit.global.enums.ScenarioCode;
import com.penfit.penfit.global.error.BusinessException;
import com.penfit.penfit.global.error.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
class PensionPlanApiTest {

    private static final String PLAN_URL = "/api/v1/users/me/pension-plan";
    private static final String PROFILE_URL = "/api/v1/users/me/financial-profile";
    private static final String SETUP_URL = "/api/v1/users/me/pension-setup";
    private static final String START_URL = "/api/v1/users/me/rehearsals";

    private static final long EXPECTED_FUTURE_ASSET = 99_871_036L;

    private static final String PROFILE_BODY = """
            {
              "ageBand": "AGE_26_28",
              "occupationType": "REGULAR_EMPLOYEE",
              "monthlySalary": 2800000,
              "livingExpenseBand": "LIVING_GT_1M_LE_1_5M",
              "assetBand": "ASSET_10M_30M",
              "debtBand": "DEBT_NONE",
              "emergencyFundBand": "EMERGENCY_1M_3M",
              "monthlySavings": 300000,
              "currentInvestment": 100000
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PensionPassportRepository pensionPassportRepository;

    @MockitoBean
    private KakaoOAuthClient kakaoOAuthClient;

    @MockitoBean
    private AiClient aiClient;

    private String accessToken;
    private long userId;

    @BeforeEach
    void setUp() throws Exception {
        accessToken = loginAs("kakao-1", "이재원");
    }

    @Test
    @DisplayName("연금계획을 만들면 자산 구성과 장점, 예상 미래자산을 함께 반환한다")
    void createsPlan() throws Exception {
        preparePassport(120_000L);
        given(aiClient.call(eq(AiApi.PENSION_PLAN), any(), eq(PensionPlanGenerateResponse.class), anyLong()))
                .willReturn(successResponse());

        mockMvc.perform(post(PLAN_URL).header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.planName").value("균형성장형"))
                .andExpect(jsonPath("$.data.accountType.code").value("PENSION_SAVINGS_FUND"))
                .andExpect(jsonPath("$.data.accountType.displayName").value("연금저축펀드"))
                .andExpect(jsonPath("$.data.monthlyContribution").value(120000))
                .andExpect(jsonPath("$.data.assetAllocation.stockRatio").value(40.0))
                .andExpect(jsonPath("$.data.assetAllocation.bondRatio").value(30.0))
                .andExpect(jsonPath("$.data.assetAllocation.depositRatio").value(30.0))
                .andExpect(jsonPath("$.data.advantages.length()").value(2))
                .andExpect(jsonPath("$.data.advantages[0]").exists())
                .andExpect(jsonPath("$.data.expectedFutureAsset").value(EXPECTED_FUTURE_ASSET))
                .andExpect(jsonPath("$.data.contributionYears").value(30));
    }

    @Test
    @DisplayName("저장한 연금계획을 다시 조회할 수 있다")
    void getsPlan() throws Exception {
        preparePassport(120_000L);
        given(aiClient.call(eq(AiApi.PENSION_PLAN), any(), eq(PensionPlanGenerateResponse.class), anyLong()))
                .willReturn(successResponse());
        mockMvc.perform(post(PLAN_URL).header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
                .andExpect(status().isCreated());

        mockMvc.perform(get(PLAN_URL).header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.planName").value("균형성장형"))
                .andExpect(jsonPath("$.data.advantages.length()").value(2))
                .andExpect(jsonPath("$.data.expectedReturnRate").value(0.0500));
    }

    @Test
    @DisplayName("패스포트가 없으면 연금계획을 만들 수 없다")
    void requiresPassport() throws Exception {
        prepareSetup();

        mockMvc.perform(post(PLAN_URL).header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PP4041"));
    }

    @Test
    @DisplayName("금융정보가 없으면 연금계획을 만들 수 없다")
    void requiresFinancialProfile() throws Exception {
        mockMvc.perform(post(PLAN_URL).header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("FP4041"));
    }

    @Test
    @DisplayName("가상 연금 설정이 없으면 연금계획을 만들 수 없다")
    void requiresPensionSetup() throws Exception {
        mockMvc.perform(post(PROFILE_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(PROFILE_BODY))
                .andExpect(status().isCreated());

        mockMvc.perform(post(PLAN_URL).header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PS4041"));
    }

    @Test
    @DisplayName("이미 계획이 있으면 새로 만들지 않는다")
    void rejectsDuplicatePlan() throws Exception {
        preparePassport(120_000L);
        given(aiClient.call(eq(AiApi.PENSION_PLAN), any(), eq(PensionPlanGenerateResponse.class), anyLong()))
                .willReturn(successResponse());
        mockMvc.perform(post(PLAN_URL).header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
                .andExpect(status().isCreated());

        mockMvc.perform(post(PLAN_URL).header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("PN4092"));
    }

    @Test
    @DisplayName("AI 시간이 초과되면 계획을 저장하지 않는다")
    void doesNotSaveOnTimeout() throws Exception {
        preparePassport(120_000L);
        willThrow(new BusinessException(ErrorCode.AI_TIMEOUT))
                .given(aiClient).call(eq(AiApi.PENSION_PLAN), any(),
                        eq(PensionPlanGenerateResponse.class), anyLong());

        mockMvc.perform(post(PLAN_URL).header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
                .andExpect(status().isGatewayTimeout())
                .andExpect(jsonPath("$.code").value("AI5041"));

        mockMvc.perform(get(PLAN_URL).header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PN4041"));
    }

    @Test
    @DisplayName("자산 비중의 합이 100이 아니면 계획을 저장하지 않는다")
    void rejectsInvalidAllocation() throws Exception {
        preparePassport(120_000L);
        given(aiClient.call(eq(AiApi.PENSION_PLAN), any(), eq(PensionPlanGenerateResponse.class), anyLong()))
                .willReturn(withAllocation(successResponse(), 50, 40, 20));

        mockMvc.perform(post(PLAN_URL).header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.code").value("AI5021"));

        mockMvc.perform(get(PLAN_URL).header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("장점이 2개보다 적으면 계획을 저장하지 않는다")
    void rejectsWrongAdvantageCount() throws Exception {
        preparePassport(120_000L);
        PensionPlanGenerateResponse source = successResponse();
        given(aiClient.call(eq(AiApi.PENSION_PLAN), any(), eq(PensionPlanGenerateResponse.class), anyLong()))
                .willReturn(withAdvantages(source, List.of("장점 하나뿐")));

        mockMvc.perform(post(PLAN_URL).header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.code").value("AI5021"));
    }

    @Test
    @DisplayName("저장된 계획이 없으면 조회할 수 없다")
    void rejectsGetWithoutPlan() throws Exception {
        mockMvc.perform(get(PLAN_URL).header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PN4041"));
    }

    @Test
    @DisplayName("토큰이 없으면 연금계획을 조회할 수 없다")
    void rejectsWithoutToken() throws Exception {
        mockMvc.perform(get(PLAN_URL))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("유지 가능액이 0원이면 AI 를 호출하지 않고 안내를 반환한다")
    void rejectsZeroSustainableContribution() throws Exception {
        preparePassport(0L);

        mockMvc.perform(post(PLAN_URL).header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("PN4221"));

        org.mockito.Mockito.verifyNoInteractions(aiClient);
    }

    @Test
    @DisplayName("한 자산에 100을 몰아도 합이 100이면 저장한다")
    void acceptsSingleAssetAllocation() throws Exception {
        preparePassport(120_000L);
        given(aiClient.call(eq(AiApi.PENSION_PLAN), any(), eq(PensionPlanGenerateResponse.class), anyLong()))
                .willReturn(withAllocation(successResponse(), 0, 0, 100));

        mockMvc.perform(post(PLAN_URL).header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.assetAllocation.depositRatio").value(100.0))
                .andExpect(jsonPath("$.data.assetAllocation.stockRatio").value(0.0));
    }

    private PensionPlanGenerateResponse successResponse() throws Exception {
        return objectMapper.readValue(
                objectMapper.readTree(new ClassPathResource("fixtures/penfit-ai-success-responses.json")
                        .getInputStream()).path("pensionPlanGenerate").traverse(),
                PensionPlanGenerateResponse.class);
    }

    private PensionPlanGenerateResponse withAllocation(PensionPlanGenerateResponse source,
                                                       int stock, int bond, int deposit) {
        return new PensionPlanGenerateResponse(source.title(), source.monthlyContribution(),
                new PensionPlanGenerateResponse.Allocation(
                        BigDecimal.valueOf(stock), BigDecimal.valueOf(bond), BigDecimal.valueOf(deposit)),
                source.targetAccountType(), source.advantages(),
                source.recommendationReason(), source.modelVersion());
    }

    private PensionPlanGenerateResponse withAdvantages(PensionPlanGenerateResponse source,
                                                       List<String> advantages) {
        return new PensionPlanGenerateResponse(source.title(), source.monthlyContribution(),
                source.allocation(), source.targetAccountType(), advantages,
                source.recommendationReason(), source.modelVersion());
    }

    private void preparePassport(long sustainableMonthlyContribution) throws Exception {
        long rehearsalId = prepareSetup();
        pensionPassportRepository.save(PensionPassport.builder()
                .userId(userId)
                .rehearsalId(rehearsalId)
                .typeCode(PassportTypeCode.STEADY_PIONEER)
                .sustainableMonthlyContribution(sustainableMonthlyContribution)
                .biggestInterruptionRiskCode(ScenarioCode.HOME_PURCHASE)
                .marketRiskLevel(MarketRiskLevel.MEDIUM)
                .summary("6가지 상황에서도 꾸준히 납입을 유지하려는 경향이 있어요.")
                .judgmentReason("계좌를 해지하기보다 납입액을 조정하는 선택을 우선했어요.")
                .detailedAnalysisReport("상황별 선택을 종합한 분석이에요.")
                .aiRawResponse("{}")
                .modelVersion("passport-model-1.0")
                .build());
    }

    private long prepareSetup() throws Exception {
        mockMvc.perform(post(PROFILE_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(PROFILE_BODY))
                .andExpect(status().isCreated());
        mockMvc.perform(post(SETUP_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"accountType\":\"PENSION_SAVINGS_FUND\",\"monthlyContribution\":100000}"))
                .andExpect(status().isCreated());

        String body = mockMvc.perform(post(START_URL).header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).path("data").path("rehearsalId").asLong();
    }

    private String loginAs(String kakaoId, String nickname) throws Exception {
        given(kakaoOAuthClient.fetchUserInfo(anyString()))
                .willReturn(new KakaoUserInfo(kakaoId, nickname));

        String body = mockMvc.perform(post("/api/v1/auth/kakao/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"authorization-code\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        userId = objectMapper.readTree(body).path("data").path("userId").asLong();
        return objectMapper.readTree(body).path("data").path("accessToken").asText();
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
