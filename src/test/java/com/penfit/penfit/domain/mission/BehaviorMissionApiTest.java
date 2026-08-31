package com.penfit.penfit.domain.mission;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.penfit.penfit.domain.passport.entity.PensionPassport;
import com.penfit.penfit.domain.passport.repository.PensionPassportRepository;
import com.penfit.penfit.domain.pensionplan.entity.PensionPlan;
import com.penfit.penfit.domain.pensionplan.repository.PensionPlanRepository;
import com.penfit.penfit.global.client.ai.AiApi;
import com.penfit.penfit.global.client.ai.AiClient;
import com.penfit.penfit.global.client.ai.dto.SpendingMissionAnalyzeResponse;
import com.penfit.penfit.global.client.kakao.KakaoOAuthClient;
import com.penfit.penfit.global.client.kakao.KakaoUserInfo;
import com.penfit.penfit.global.enums.AccountType;
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
import java.time.LocalDate;
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
class BehaviorMissionApiTest {

    private static final String ANALYSIS_URL = "/api/v1/users/me/spending-analysis";
    private static final String CURRENT_URL = "/api/v1/users/me/behavior-missions/current";
    private static final String COMPLETIONS_URL = "/api/v1/users/me/behavior-missions/completions";
    private static final String PROFILE_URL = "/api/v1/users/me/financial-profile";
    private static final String SETUP_URL = "/api/v1/users/me/pension-setup";
    private static final String START_URL = "/api/v1/users/me/rehearsals";

    private static final long TOTAL_SPENDING = 257_000L;
    private static final long PENSION_IMPACT = 12_483_880L;

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

    @Autowired
    private PensionPlanRepository pensionPlanRepository;

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
    @DisplayName("소비 분석은 지출이 없는 영역까지 다섯 개를 반환한다")
    void analyzesSpending() throws Exception {
        preparePlan();
        givenAiReturns(successResponse());

        mockMvc.perform(post(ANALYSIS_URL).header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.topCategory.code").value("FOOD_DELIVERY"))
                .andExpect(jsonPath("$.data.topCategory.displayName").value("외식·배달"))
                .andExpect(jsonPath("$.data.totalAmount").value(TOTAL_SPENDING))
                .andExpect(jsonPath("$.data.recurringExpense").value(19000))
                .andExpect(jsonPath("$.data.reducibleAmount").value(60000))
                .andExpect(jsonPath("$.data.categorySpending.length()").value(5))
                .andExpect(jsonPath("$.data.categorySpending[0].category.code").value("FOOD_DELIVERY"))
                .andExpect(jsonPath("$.data.categorySpending[0].amount").value(140000))
                .andExpect(jsonPath("$.data.categorySpending[4].category.code").value("OTHER"))
                .andExpect(jsonPath("$.data.categorySpending[4].amount").value(15000))
                .andExpect(jsonPath("$.data.keyInsights.length()").value(3))
                .andExpect(jsonPath("$.data.analysisStartDate").value("2026-08-24"))
                .andExpect(jsonPath("$.data.analysisEndDate").value("2026-08-30"));
    }

    @Test
    @DisplayName("저장한 소비 분석을 다시 조회할 수 있다")
    void getsAnalysis() throws Exception {
        analyze();

        mockMvc.perform(get(ANALYSIS_URL).header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.categorySpending.length()").value(5))
                .andExpect(jsonPath("$.data.summary").exists());
    }

    @Test
    @DisplayName("현재 미션은 목표와 마감, 남은 기한을 함께 반환한다")
    void getsCurrentMission() throws Exception {
        analyze();

        mockMvc.perform(get(CURRENT_URL).header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("커피값 아껴서 연금 넣어보기"))
                .andExpect(jsonPath("$.data.targetAmount").value(15000))
                .andExpect(jsonPath("$.data.durationDays").value(7))
                .andExpect(jsonPath("$.data.daysLeft").value(7))
                .andExpect(jsonPath("$.data.dueDate").value(LocalDate.now().plusDays(7).toString()))
                .andExpect(jsonPath("$.data.status.code").value("PENDING"))
                .andExpect(jsonPath("$.data.topCategory.code").value("FOOD_DELIVERY"))
                .andExpect(jsonPath("$.data.topCategoryRatio").value(54.47));
    }

    @Test
    @DisplayName("미션을 시작하면 진행 중이 된다")
    void startsMission() throws Exception {
        long missionId = analyzeAndGetMissionId();

        mockMvc.perform(post(missionUrl(missionId, "start"))
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status.code").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.data.startedAt").exists());
    }

    @Test
    @DisplayName("미션을 완료하면 예상 연금자산 증가분을 계산해 반환한다")
    void completesMission() throws Exception {
        long missionId = analyzeAndGetMissionId();

        mockMvc.perform(post(missionUrl(missionId, "complete"))
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status.code").value("COMPLETED"))
                .andExpect(jsonPath("$.data.completedAt").exists())
                .andExpect(jsonPath("$.data.pensionImpactAmount").value(PENSION_IMPACT));
    }

    @Test
    @DisplayName("완료 이력은 확보 금액과 연금 영향을 합산해 반환한다")
    void getsCompletions() throws Exception {
        long missionId = analyzeAndGetMissionId();
        mockMvc.perform(post(missionUrl(missionId, "complete"))
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
                .andExpect(status().isOk());

        mockMvc.perform(get(COMPLETIONS_URL)
                        .param("year", String.valueOf(LocalDate.now().getYear()))
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.completedCount").value(1))
                .andExpect(jsonPath("$.data.totalSavedAmount").value(15000))
                .andExpect(jsonPath("$.data.totalPensionImpactAmount").value(PENSION_IMPACT))
                .andExpect(jsonPath("$.data.completions[0].title").value("커피값 아껴서 연금 넣어보기"))
                .andExpect(jsonPath("$.data.completions[0].completedDate")
                        .value(LocalDate.now(java.time.ZoneId.of("Asia/Seoul")).toString()));
    }

    @Test
    @DisplayName("연금계획이 없으면 소비 분석을 할 수 없다")
    void requiresPensionPlan() throws Exception {
        mockMvc.perform(post(ANALYSIS_URL).header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PN4041"));
    }

    @Test
    @DisplayName("분석 결과가 없으면 조회할 수 없다")
    void rejectsAnalysisWhenMissing() throws Exception {
        mockMvc.perform(get(ANALYSIS_URL).header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("BM4042"));
    }

    @Test
    @DisplayName("미션이 없으면 조회할 수 없다")
    void rejectsMissionWhenMissing() throws Exception {
        mockMvc.perform(get(CURRENT_URL).header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("BM4041"));
    }

    @Test
    @DisplayName("이미 시작한 미션은 다시 시작할 수 없다")
    void rejectsDuplicateStart() throws Exception {
        long missionId = analyzeAndGetMissionId();
        mockMvc.perform(post(missionUrl(missionId, "start"))
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
                .andExpect(status().isOk());

        mockMvc.perform(post(missionUrl(missionId, "start"))
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("BM4091"));
    }

    @Test
    @DisplayName("이미 완료한 미션은 다시 완료할 수 없다")
    void rejectsDuplicateComplete() throws Exception {
        long missionId = analyzeAndGetMissionId();
        mockMvc.perform(post(missionUrl(missionId, "complete"))
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
                .andExpect(status().isOk());

        mockMvc.perform(post(missionUrl(missionId, "complete"))
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("BM4092"));
    }

    @Test
    @DisplayName("남의 미션은 완료할 수 없다")
    void rejectsOtherUsersMission() throws Exception {
        long missionId = analyzeAndGetMissionId();
        String otherToken = loginAs("kakao-2", "김하늘");

        mockMvc.perform(post(missionUrl(missionId, "complete"))
                        .header(HttpHeaders.AUTHORIZATION, bearer(otherToken)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("BM4041"));
    }

    @Test
    @DisplayName("핵심 소비가 3개가 아니면 저장하지 않는다")
    void rejectsWrongInsightCount() throws Exception {
        preparePlan();
        SpendingMissionAnalyzeResponse source = successResponse();
        givenAiReturns(withInsights(source, List.of("하나뿐인 문장")));

        mockMvc.perform(post(ANALYSIS_URL).header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.code").value("AI5021"));

        mockMvc.perform(get(ANALYSIS_URL).header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("목표 금액이 5천원 단위가 아니면 저장하지 않는다")
    void rejectsInvalidTargetAmount() throws Exception {
        preparePlan();
        givenAiReturns(withTargetAmount(successResponse(), 33_000L));

        mockMvc.perform(post(ANALYSIS_URL).header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.code").value("AI5021"));
    }

    @Test
    @DisplayName("절감할 수 있는 지출이 없으면 BM4221 을 반환한다")
    void handlesNoActionableSpending() throws Exception {
        preparePlan();
        willThrow(new BusinessException(ErrorCode.NO_ACTIONABLE_SPENDING))
                .given(aiClient).call(eq(AiApi.SPENDING_MISSION), any(),
                        eq(SpendingMissionAnalyzeResponse.class), anyLong());

        mockMvc.perform(post(ANALYSIS_URL).header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("BM4221"));
    }

    @Test
    @DisplayName("목표 금액이 최소값이어도 저장한다")
    void acceptsMinimumTargetAmount() throws Exception {
        preparePlan();
        givenAiReturns(withTargetAmount(successResponse(), 5_000L));

        mockMvc.perform(post(ANALYSIS_URL).header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
                .andExpect(status().isCreated());

        mockMvc.perform(get(CURRENT_URL).header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.targetAmount").value(5000));
    }

    @Test
    @DisplayName("토큰이 없으면 현재 미션을 조회할 수 없다")
    void rejectsWithoutToken() throws Exception {
        mockMvc.perform(get(CURRENT_URL))
                .andExpect(status().isUnauthorized());
    }

    private long analyzeAndGetMissionId() throws Exception {
        analyze();
        String body = mockMvc.perform(get(CURRENT_URL).header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).path("data").path("missionId").asLong();
    }

    private void analyze() throws Exception {
        preparePlan();
        givenAiReturns(successResponse());
        mockMvc.perform(post(ANALYSIS_URL).header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
                .andExpect(status().isCreated());
    }

    private void givenAiReturns(SpendingMissionAnalyzeResponse response) {
        given(aiClient.call(eq(AiApi.SPENDING_MISSION), any(),
                eq(SpendingMissionAnalyzeResponse.class), anyLong()))
                .willReturn(response);
    }

    private SpendingMissionAnalyzeResponse successResponse() throws Exception {
        return objectMapper.readValue(
                objectMapper.readTree(new ClassPathResource("fixtures/penfit-ai-success-responses.json")
                        .getInputStream()).path("spendingMissionAnalyze").traverse(),
                SpendingMissionAnalyzeResponse.class);
    }

    private SpendingMissionAnalyzeResponse withInsights(SpendingMissionAnalyzeResponse source,
                                                        List<String> insights) {
        SpendingMissionAnalyzeResponse.SpendingAnalysisPayload analysis = source.spendingAnalysis();
        return new SpendingMissionAnalyzeResponse(
                new SpendingMissionAnalyzeResponse.SpendingAnalysisPayload(
                        analysis.topCategoryCode(), analysis.recurringExpense(), analysis.reducibleAmount(),
                        analysis.categorySpending(), insights, analysis.summary()),
                source.mission(), source.modelVersion());
    }

    private SpendingMissionAnalyzeResponse withTargetAmount(SpendingMissionAnalyzeResponse source,
                                                            long targetAmount) {
        SpendingMissionAnalyzeResponse.MissionPayload mission = source.mission();
        return new SpendingMissionAnalyzeResponse(source.spendingAnalysis(),
                new SpendingMissionAnalyzeResponse.MissionPayload(
                        mission.title(), mission.description(), targetAmount,
                        mission.durationDays(), mission.reason()),
                source.modelVersion());
    }

    private void preparePlan() throws Exception {
        if (pensionPlanRepository.existsByUserId(userId)) {
            return;
        }
        long rehearsalId = prepareRehearsal();
        PensionPassport passport = pensionPassportRepository.save(PensionPassport.builder()
                .userId(userId)
                .rehearsalId(rehearsalId)
                .typeCode(PassportTypeCode.STEADY_PIONEER)
                .sustainableMonthlyContribution(120_000L)
                .biggestInterruptionRiskCode(ScenarioCode.HOME_PURCHASE)
                .marketRiskLevel(MarketRiskLevel.MEDIUM)
                .summary("꾸준히 납입을 유지하려는 경향이 있어요.")
                .judgmentReason("계좌를 해지하기보다 납입액을 조정하는 선택을 우선했어요.")
                .detailedAnalysisReport("상황별 선택을 종합한 분석이에요.")
                .aiRawResponse("{}")
                .modelVersion("passport-model-1.0")
                .build());

        pensionPlanRepository.save(PensionPlan.builder()
                .userId(userId)
                .passportId(passport.getId())
                .planName("균형 있게 시작")
                .accountType(AccountType.PENSION_SAVINGS_FUND)
                .monthlyContribution(120_000L)
                .stockRatio(BigDecimal.valueOf(50))
                .bondRatio(BigDecimal.valueOf(40))
                .depositRatio(BigDecimal.valueOf(10))
                .recommendationReason("장기 성장을 기대할 수 있는 구성이에요.")
                .expectedFutureAsset(99_871_036L)
                .contributionYears(30)
                .expectedReturnRate(BigDecimal.valueOf(0.05))
                .aiRawResponse("{}")
                .modelVersion("pension-plan-model-1.0")
                .build());
    }

    private long prepareRehearsal() throws Exception {
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

    private String missionUrl(long missionId, String action) {
        return "/api/v1/users/me/behavior-missions/%d/%s".formatted(missionId, action);
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
