package com.penfit.penfit.domain.home;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.penfit.penfit.domain.mission.entity.BehaviorMission;
import com.penfit.penfit.domain.mission.entity.SpendingAnalysis;
import com.penfit.penfit.domain.mission.repository.BehaviorMissionRepository;
import com.penfit.penfit.domain.mission.repository.SpendingAnalysisRepository;
import com.penfit.penfit.domain.passport.entity.PensionPassport;
import com.penfit.penfit.domain.passport.repository.PensionPassportRepository;
import com.penfit.penfit.domain.pensionplan.entity.PensionPlan;
import com.penfit.penfit.domain.pensionplan.repository.PensionPlanRepository;
import com.penfit.penfit.domain.product.entity.PensionProduct;
import com.penfit.penfit.domain.product.repository.PensionProductRepository;
import com.penfit.penfit.global.client.kakao.KakaoOAuthClient;
import com.penfit.penfit.global.client.kakao.KakaoUserInfo;
import com.penfit.penfit.global.enums.AccountType;
import com.penfit.penfit.global.enums.CategoryCode;
import com.penfit.penfit.global.enums.MarketRiskLevel;
import com.penfit.penfit.global.enums.PassportTypeCode;
import com.penfit.penfit.global.enums.ScenarioCode;
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

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
class HomeApiTest {

    private static final String HOME_URL = "/api/v1/users/me/home";
    private static final String PROFILE_URL = "/api/v1/users/me/financial-profile";
    private static final String SETUP_URL = "/api/v1/users/me/pension-setup";
    private static final String START_URL = "/api/v1/users/me/rehearsals";

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

    @Autowired
    private SpendingAnalysisRepository spendingAnalysisRepository;

    @Autowired
    private BehaviorMissionRepository behaviorMissionRepository;

    @Autowired
    private PensionProductRepository pensionProductRepository;

    @MockitoBean
    private KakaoOAuthClient kakaoOAuthClient;

    private String accessToken;
    private long userId;

    @BeforeEach
    void setUp() throws Exception {
        accessToken = loginAs("kakao-1", "이재원");
    }

    @Test
    @DisplayName("메인은 닉네임과 카드 네 개를 한 번에 반환한다")
    void returnsAllCards() throws Exception {
        PensionPassport passport = savePassport();
        savePlan(passport.getId());
        saveMission();
        saveProduct();

        mockMvc.perform(get(HOME_URL).header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nickname").value("이재원"))
                .andExpect(jsonPath("$.data.passport.type.code").value("STEADY_PIONEER"))
                .andExpect(jsonPath("$.data.passport.type.displayName").value("성실한 개척자형"))
                .andExpect(jsonPath("$.data.passport.typeSummary")
                        .value("시장을 믿고 꾸준히 밀고 나가는 성향이에요"))
                .andExpect(jsonPath("$.data.pensionPlan.monthlyContribution").value(120000))
                .andExpect(jsonPath("$.data.pensionPlan.accountType.displayName").value("연금저축펀드"))
                .andExpect(jsonPath("$.data.pensionPlan.expectedFutureAsset").value(99871036))
                .andExpect(jsonPath("$.data.mission.title").value("커피값 아껴서 연금 넣어보기"))
                .andExpect(jsonPath("$.data.mission.targetAmount").value(30000))
                .andExpect(jsonPath("$.data.mission.daysLeft").value(7))
                .andExpect(jsonPath("$.data.mission.status.code").value("PENDING"))
                .andExpect(jsonPath("$.data.savedProducts.length()").value(1))
                .andExpect(jsonPath("$.data.savedProducts[0].productName").value("미래에셋 연금저축펀드"))
                .andExpect(jsonPath("$.data.savedProducts[0].providerName").value("미래에셋증권"))
                .andExpect(jsonPath("$.data.savedProducts[0].feeMinRate").value(0.0015));
    }

    @Test
    @DisplayName("아무것도 하지 않은 사용자도 메인을 볼 수 있다")
    void returnsEmptyCards() throws Exception {
        mockMvc.perform(get(HOME_URL).header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nickname").value("이재원"))
                .andExpect(jsonPath("$.data.passport").doesNotExist())
                .andExpect(jsonPath("$.data.pensionPlan").doesNotExist())
                .andExpect(jsonPath("$.data.mission").doesNotExist())
                .andExpect(jsonPath("$.data.savedProducts.length()").value(0));
    }

    @Test
    @DisplayName("패스포트만 있으면 나머지 카드는 비어 있다")
    void returnsPartialCards() throws Exception {
        savePassport();

        mockMvc.perform(get(HOME_URL).header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.passport.type.code").value("STEADY_PIONEER"))
                .andExpect(jsonPath("$.data.pensionPlan").doesNotExist())
                .andExpect(jsonPath("$.data.mission").doesNotExist())
                .andExpect(jsonPath("$.data.savedProducts.length()").value(0));
    }

    @Test
    @DisplayName("다른 사용자가 담은 상품은 내 메인에 보이지 않는다")
    void isolatesSavedProducts() throws Exception {
        String otherToken = loginAs("kakao-2", "김하늘");
        saveProductWith(otherToken);

        mockMvc.perform(get(HOME_URL).header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.savedProducts.length()").value(0));
    }

    @Test
    @DisplayName("토큰이 없으면 메인을 조회할 수 없다")
    void rejectsWithoutToken() throws Exception {
        mockMvc.perform(get(HOME_URL))
                .andExpect(status().isUnauthorized());
    }

    private PensionPassport savePassport() throws Exception {
        long rehearsalId = prepareRehearsal();
        return pensionPassportRepository.save(PensionPassport.builder()
                .userId(userId)
                .rehearsalId(rehearsalId)
                .typeCode(PassportTypeCode.STEADY_PIONEER)
                .sustainableMonthlyContribution(120_000L)
                .biggestInterruptionRiskCode(ScenarioCode.HOME_PURCHASE)
                .marketRiskLevel(MarketRiskLevel.MEDIUM)
                .typeSummary("시장을 믿고 꾸준히 밀고 나가는 성향이에요")
                .summary("꾸준히 납입을 유지하려는 경향이 있어요.")
                .judgmentReason("계좌를 해지하기보다 납입액을 조정하는 선택을 우선했어요.")
                .aiRawResponse("{}")
                .modelVersion("passport-model-1.0")
                .build());
    }

    private void savePlan(Long passportId) {
        pensionPlanRepository.save(PensionPlan.builder()
                .userId(userId)
                .passportId(passportId)
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

    private void saveMission() {
        SpendingAnalysis analysis = spendingAnalysisRepository.save(SpendingAnalysis.builder()
                .userId(userId)
                .analysisStartDate(LocalDate.of(2026, 7, 1))
                .analysisEndDate(LocalDate.of(2026, 7, 28))
                .topCategoryCode(CategoryCode.FOOD_DELIVERY)
                .recurringExpense(10_000L)
                .reducibleAmount(120_000L)
                .summary("외식·배달 지출 비중이 가장 높아요.")
                .aiRawResponse("{}")
                .modelVersion("spending-mission-model-1.0")
                .build());

        behaviorMissionRepository.save(BehaviorMission.builder()
                .userId(userId)
                .spendingAnalysisId(analysis.getId())
                .title("커피값 아껴서 연금 넣어보기")
                .description("이번 주 3만원 아껴서 연금계좌 추가 납입")
                .reason("외식·배달 지출이 가장 큰 비중을 차지해요.")
                .targetAmount(30_000L)
                .durationDays(7)
                .dueDate(LocalDate.now(java.time.ZoneId.of("Asia/Seoul")).plusDays(7))
                .modelVersion("spending-mission-model-1.0")
                .build());
    }

    private void saveProduct() throws Exception {
        saveProductWith(accessToken);
    }

    private void saveProductWith(String token) throws Exception {
        PensionProduct product = pensionProductRepository.findAll().stream()
                .filter(item -> item.getProductName().equals("미래에셋 연금저축펀드"))
                .findFirst()
                .orElseThrow();

        mockMvc.perform(post("/api/v1/users/me/saved-products/" + product.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isCreated());
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

    private String loginAs(String kakaoId, String nickname) throws Exception {
        given(kakaoOAuthClient.fetchUserInfo(anyString()))
                .willReturn(new KakaoUserInfo(kakaoId, nickname));

        String body = mockMvc.perform(post("/api/v1/auth/kakao/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"authorization-code\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        if (kakaoId.equals("kakao-1")) {
            userId = objectMapper.readTree(body).path("data").path("userId").asLong();
        }
        return objectMapper.readTree(body).path("data").path("accessToken").asText();
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
