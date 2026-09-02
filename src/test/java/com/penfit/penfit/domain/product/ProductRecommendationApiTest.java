package com.penfit.penfit.domain.product;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.penfit.penfit.domain.passport.entity.PensionPassport;
import com.penfit.penfit.domain.passport.repository.PensionPassportRepository;
import com.penfit.penfit.domain.pensionplan.entity.PensionPlan;
import com.penfit.penfit.domain.pensionplan.repository.PensionPlanRepository;
import com.penfit.penfit.domain.product.entity.PensionProduct;
import com.penfit.penfit.domain.product.repository.PensionProductRepository;
import com.penfit.penfit.global.client.ai.AiApi;
import com.penfit.penfit.global.client.ai.AiClient;
import com.penfit.penfit.global.client.ai.dto.ProductRecommendationGenerateRequest;
import com.penfit.penfit.global.client.ai.dto.ProductRecommendationGenerateResponse;
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
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
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

import static org.assertj.core.api.Assertions.assertThat;
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
class ProductRecommendationApiTest {

    private static final String RECOMMENDATION_URL = "/api/v1/users/me/product-recommendations";
    private static final String COMPARISON_URL = RECOMMENDATION_URL + "/comparison";
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
    private PensionProductRepository pensionProductRepository;

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
    @DisplayName("추천 3개를 순위 순서로 저장하고 반환한다")
    void createsRecommendations() throws Exception {
        List<PensionProduct> candidates = preparePlan(AccountType.PENSION_SAVINGS_FUND);
        givenAiReturns(candidates);

        mockMvc.perform(post(RECOMMENDATION_URL).header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.length()").value(3))
                .andExpect(jsonPath("$.data[0].rank").value(1))
                .andExpect(jsonPath("$.data[0].productId").value(candidates.get(0).getId()))
                .andExpect(jsonPath("$.data[0].productName").value(candidates.get(0).getProductName()))
                .andExpect(jsonPath("$.data[0].fitLevel.code").value("VERY_HIGH"))
                .andExpect(jsonPath("$.data[0].fitLevel.displayName").value("매우 높음"))
                .andExpect(jsonPath("$.data[0].accountType.displayName").value("연금저축펀드"))
                .andExpect(jsonPath("$.data[2].rank").value(3))
                .andExpect(jsonPath("$.data[2].fitLevel.code").value("HIGH"));
    }

    @Test
    @DisplayName("저장한 추천 목록을 다시 조회할 수 있다")
    void getsRecommendations() throws Exception {
        createRecommendations();

        mockMvc.perform(get(RECOMMENDATION_URL).header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(3))
                .andExpect(jsonPath("$.data[0].rank").value(1))
                .andExpect(jsonPath("$.data[0].recommendationReason").exists());
    }

    @Test
    @DisplayName("비교는 수수료와 투자 범위, 적합도를 함께 반환한다")
    void getsComparison() throws Exception {
        List<PensionProduct> candidates = createRecommendations();

        mockMvc.perform(get(COMPARISON_URL).header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.products.length()").value(3))
                .andExpect(jsonPath("$.data.products[0].providerName")
                        .value(candidates.get(0).getProviderName()))
                .andExpect(jsonPath("$.data.products[0].investmentScope")
                        .value(candidates.get(0).getInvestmentScope()))
                .andExpect(jsonPath("$.data.products[0].feeMinRate").value(0.0015))
                .andExpect(jsonPath("$.data.products[0].fitLevel.displayName").value("매우 높음"));
    }

    @Test
    @DisplayName("AI 요청의 수수료율은 퍼센트 숫자로 변환해 보낸다")
    void sendsFeeRateAsPercent() throws Exception {
        List<PensionProduct> candidates = preparePlan(AccountType.PENSION_SAVINGS_FUND);
        givenAiReturns(candidates);

        mockMvc.perform(post(RECOMMENDATION_URL).header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
                .andExpect(status().isCreated());

        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        Mockito.verify(aiClient).call(eq(AiApi.PRODUCT_RECOMMENDATION), captor.capture(),
                eq(ProductRecommendationGenerateResponse.class), anyLong());

        ProductRecommendationGenerateRequest request =
                (ProductRecommendationGenerateRequest) captor.getValue();
        assertThat(request.products()).hasSize(5);
        assertThat(request.plan().targetAccountType()).isEqualTo("PENSION_SAVINGS_FUND");
        assertThat(request.products().get(0).feeMinRate())
                .isEqualByComparingTo(BigDecimal.valueOf(0.15));
        assertThat(request.products().get(0).feeMaxRate())
                .isEqualByComparingTo(BigDecimal.valueOf(0.45));
    }

    @Test
    @DisplayName("연금계획이 없으면 추천을 만들 수 없다")
    void requiresPensionPlan() throws Exception {
        mockMvc.perform(post(RECOMMENDATION_URL).header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PN4041"));
    }

    @Test
    @DisplayName("후보 상품이 3개 미만이면 AI 를 호출하지 않는다")
    void rejectsInsufficientCandidates() throws Exception {
        preparePlan(AccountType.PENSION_SAVINGS_INSURANCE);
        List<PensionProduct> insurance = pensionProductRepository
                .findAllByAccountTypeAndIsActiveTrueOrderByIdAsc(AccountType.PENSION_SAVINGS_INSURANCE);
        pensionProductRepository.deleteAll(insurance.subList(0, insurance.size() - 2));
        pensionProductRepository.flush();

        mockMvc.perform(post(RECOMMENDATION_URL).header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("PR4221"));

        Mockito.verifyNoInteractions(aiClient);
    }

    @Test
    @DisplayName("AI 가 추천 3개를 만들지 못하면 PR4222 를 반환한다")
    void handlesInsufficientRecommendations() throws Exception {
        preparePlan(AccountType.PENSION_SAVINGS_FUND);
        willThrow(new BusinessException(ErrorCode.INSUFFICIENT_RECOMMENDATIONS))
                .given(aiClient).call(eq(AiApi.PRODUCT_RECOMMENDATION), any(),
                        eq(ProductRecommendationGenerateResponse.class), anyLong());

        mockMvc.perform(post(RECOMMENDATION_URL).header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("PR4222"));
    }

    @Test
    @DisplayName("후보에 없는 상품을 추천하면 저장하지 않는다")
    void rejectsUnknownProduct() throws Exception {
        List<PensionProduct> candidates = preparePlan(AccountType.PENSION_SAVINGS_FUND);
        ProductRecommendationGenerateResponse response = responseFor(candidates);
        List<ProductRecommendationGenerateResponse.RecommendedProduct> tampered = List.of(
                withProductId(response.recommendedProducts().get(0), 999_999L),
                response.recommendedProducts().get(1),
                response.recommendedProducts().get(2));
        given(aiClient.call(eq(AiApi.PRODUCT_RECOMMENDATION), any(),
                eq(ProductRecommendationGenerateResponse.class), anyLong()))
                .willReturn(new ProductRecommendationGenerateResponse(tampered, response.modelVersion()));

        mockMvc.perform(post(RECOMMENDATION_URL).header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.code").value("AI5021"));

        mockMvc.perform(get(RECOMMENDATION_URL).header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PR4042"));
    }

    @Test
    @DisplayName("추천이 없으면 목록을 조회할 수 없다")
    void rejectsListWithoutRecommendations() throws Exception {
        mockMvc.perform(get(RECOMMENDATION_URL).header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PR4042"));
    }

    @Test
    @DisplayName("추천이 없으면 비교할 수 없다")
    void rejectsComparisonWithoutRecommendations() throws Exception {
        mockMvc.perform(get(COMPARISON_URL).header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PR4042"));
    }

    @Test
    @DisplayName("토큰이 없으면 추천을 조회할 수 없다")
    void rejectsWithoutToken() throws Exception {
        mockMvc.perform(get(RECOMMENDATION_URL))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("다시 추천하면 기존 결과를 새 결과로 바꾼다")
    void replacesRecommendations() throws Exception {
        List<PensionProduct> candidates = createRecommendations();

        ProductRecommendationGenerateResponse second = responseFor(
                List.of(candidates.get(3), candidates.get(4), candidates.get(0)));
        given(aiClient.call(eq(AiApi.PRODUCT_RECOMMENDATION), any(),
                eq(ProductRecommendationGenerateResponse.class), anyLong()))
                .willReturn(second);

        mockMvc.perform(post(RECOMMENDATION_URL).header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data[0].productId").value(candidates.get(3).getId()));

        mockMvc.perform(get(RECOMMENDATION_URL).header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(3))
                .andExpect(jsonPath("$.data[0].productId").value(candidates.get(3).getId()));
    }

    private List<PensionProduct> createRecommendations() throws Exception {
        List<PensionProduct> candidates = preparePlan(AccountType.PENSION_SAVINGS_FUND);
        givenAiReturns(candidates);
        mockMvc.perform(post(RECOMMENDATION_URL).header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
                .andExpect(status().isCreated());
        return candidates;
    }

    private void givenAiReturns(List<PensionProduct> candidates) throws Exception {
        given(aiClient.call(eq(AiApi.PRODUCT_RECOMMENDATION), any(),
                eq(ProductRecommendationGenerateResponse.class), anyLong()))
                .willReturn(responseFor(candidates));
    }

    private ProductRecommendationGenerateResponse responseFor(List<PensionProduct> candidates)
            throws Exception {
        ProductRecommendationGenerateResponse fixture = objectMapper.readValue(
                objectMapper.readTree(new ClassPathResource("fixtures/penfit-ai-success-responses.json")
                        .getInputStream()).path("productRecommendationsGenerate").traverse(),
                ProductRecommendationGenerateResponse.class);

        List<ProductRecommendationGenerateResponse.RecommendedProduct> reflected =
                java.util.stream.IntStream.range(0, fixture.recommendedProducts().size())
                        .mapToObj(index -> withProductId(fixture.recommendedProducts().get(index),
                                candidates.get(index).getId()))
                        .toList();
        return new ProductRecommendationGenerateResponse(reflected, fixture.modelVersion());
    }

    private ProductRecommendationGenerateResponse.RecommendedProduct withProductId(
            ProductRecommendationGenerateResponse.RecommendedProduct source, Long productId) {
        return new ProductRecommendationGenerateResponse.RecommendedProduct(
                source.rank(), productId, source.fitScore(), source.fitLevel(),
                source.recommendationReason());
    }

    private List<PensionProduct> preparePlan(AccountType accountType) throws Exception {
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
                .accountType(accountType)
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

        return pensionProductRepository.findAllByAccountTypeAndIsActiveTrueOrderByIdAsc(accountType);
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
        given(kakaoOAuthClient.fetchUserInfo(anyString(), any()))
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
