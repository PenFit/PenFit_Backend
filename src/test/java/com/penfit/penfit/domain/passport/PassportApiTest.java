package com.penfit.penfit.domain.passport;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.penfit.penfit.domain.passport.service.PassportAnalysisService;
import com.penfit.penfit.global.client.ai.AiApi;
import com.penfit.penfit.global.client.ai.AiClient;
import com.penfit.penfit.global.client.ai.dto.PassportAnalyzeRequest;
import com.penfit.penfit.global.client.ai.dto.PassportAnalyzeResponse;
import com.penfit.penfit.global.client.kakao.KakaoOAuthClient;
import com.penfit.penfit.global.client.kakao.KakaoUserInfo;
import com.penfit.penfit.global.error.BusinessException;
import com.penfit.penfit.global.error.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
class PassportApiTest {

    private static final String PROFILE_URL = "/api/v1/users/me/financial-profile";
    private static final String SETUP_URL = "/api/v1/users/me/pension-setup";
    private static final String START_URL = "/api/v1/users/me/rehearsals";
    private static final String PASSPORT_URL = "/api/v1/users/me/pension-passport";

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

    private static final Map<String, String> ANSWERS = new java.util.LinkedHashMap<>(Map.of());

    static {
        ANSWERS.put("JOB_CHANGE", "KEEP");
        ANSWERS.put("INDEPENDENCE", "CUT_EXPENSE_AND_KEEP");
        ANSWERS.put("MARRIAGE", "REDUCE_EVENT_COST_AND_KEEP");
        ANSWERS.put("HOME_PURCHASE", "DELAY_EVENT");
        ANSWERS.put("CHILDBIRTH", "KEEP");
        ANSWERS.put("MARKET_DOWNTURN", "REBALANCE");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PassportAnalysisService passportAnalysisService;

    @MockitoBean
    private KakaoOAuthClient kakaoOAuthClient;

    @MockitoBean
    private AiClient aiClient;

    private String accessToken;

    @BeforeEach
    void setUp() throws Exception {
        accessToken = loginAs("kakao-1", "이재원");
    }

    @Test
    @DisplayName("답변 6개를 채우면 분석을 접수하고 AI 분석 중 상태가 된다")
    void acceptsAnalysis() throws Exception {
        long rehearsalId = readyRehearsal();

        mockMvc.perform(post(completeUrl(rehearsalId)).header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.data.status.code").value("ANALYZING"))
                .andExpect(jsonPath("$.data.status.displayName").value("AI 분석 중"))
                .andExpect(jsonPath("$.data.retryCount").value(0));
    }

    @Test
    @DisplayName("분석이 끝나면 패스포트를 저장하고 리허설을 완료로 바꾼다")
    void savesPassport() throws Exception {
        long rehearsalId = analyzingRehearsal();
        given(aiClient.call(eq(AiApi.PASSPORT), any(), eq(PassportAnalyzeResponse.class), anyLong()))
                .willReturn(successResponse());

        passportAnalysisService.analyze(rehearsalId);

        mockMvc.perform(get(PASSPORT_URL).header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.type.code").value("STEADY_PIONEER"))
                .andExpect(jsonPath("$.data.type.displayName").value("성실한 개척자형"))
                .andExpect(jsonPath("$.data.sustainableMonthlyContribution").value(120000))
                .andExpect(jsonPath("$.data.biggestInterruptionRisk.code").value("HOME_PURCHASE"))
                .andExpect(jsonPath("$.data.biggestInterruptionRisk.displayName").value("주택 구매"))
                .andExpect(jsonPath("$.data.marketRiskLevel.displayName").value("중간"))
                .andExpect(jsonPath("$.data.detailedAnalysis.length()").value(6))
                .andExpect(jsonPath("$.data.detailedAnalysis[0].scenario.code").value("JOB_CHANGE"))
                .andExpect(jsonPath("$.data.detailedAnalysis[0].selectedOptionCode").value("KEEP"));

        mockMvc.perform(get(rehearsalUrl(rehearsalId)).header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status.code").value("COMPLETED"))
                .andExpect(jsonPath("$.data.completedAt").exists());
    }

    @Test
    @DisplayName("AI 요청에는 저장된 답변 6개가 담긴다")
    void sendsSixAnswers() throws Exception {
        long rehearsalId = analyzingRehearsal();
        given(aiClient.call(eq(AiApi.PASSPORT), any(), eq(PassportAnalyzeResponse.class), anyLong()))
                .willReturn(successResponse());

        passportAnalysisService.analyze(rehearsalId);

        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        org.mockito.Mockito.verify(aiClient)
                .call(eq(AiApi.PASSPORT), captor.capture(), eq(PassportAnalyzeResponse.class), anyLong());

        PassportAnalyzeRequest request = (PassportAnalyzeRequest) captor.getValue();
        assertThat(request.rehearsalAnswers()).hasSize(6);
        assertThat(request.financialProfile().ageBand()).isEqualTo("AGE_26_28");
        assertThat(request.pensionSetup().monthlyContribution()).isEqualTo(100000L);
    }

    @Test
    @DisplayName("AI 시간이 초과되면 리허설을 분석 실패로 바꾼다")
    void marksFailedOnTimeout() throws Exception {
        long rehearsalId = analyzingRehearsal();
        willThrow(new BusinessException(ErrorCode.AI_TIMEOUT))
                .given(aiClient).call(eq(AiApi.PASSPORT), any(), eq(PassportAnalyzeResponse.class), anyLong());

        passportAnalysisService.analyze(rehearsalId);

        mockMvc.perform(get(rehearsalUrl(rehearsalId)).header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status.code").value("FAILED"))
                .andExpect(jsonPath("$.data.failureCode").value("AI5041"));
    }

    @Test
    @DisplayName("세부 분석이 6개가 아니면 패스포트를 저장하지 않는다")
    void rejectsIncompleteAnalysis() throws Exception {
        long rehearsalId = analyzingRehearsal();
        PassportAnalyzeResponse response = successResponse();
        List<PassportAnalyzeResponse.DetailedAnalysis> partial =
                new ArrayList<>(response.detailedAnalysis()).subList(0, 5);
        given(aiClient.call(eq(AiApi.PASSPORT), any(), eq(PassportAnalyzeResponse.class), anyLong()))
                .willReturn(withAnalyses(response, partial));

        passportAnalysisService.analyze(rehearsalId);

        mockMvc.perform(get(rehearsalUrl(rehearsalId)).header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status.code").value("FAILED"))
                .andExpect(jsonPath("$.data.failureCode").value("AI5021"));

        mockMvc.perform(get(PASSPORT_URL).header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PP4041"));
    }

    @Test
    @DisplayName("답변이 부족하면 분석을 시작할 수 없다")
    void rejectsIncompleteAnswers() throws Exception {
        long rehearsalId = startRehearsal();
        answer(rehearsalId, "JOB_CHANGE", "KEEP");

        mockMvc.perform(post(completeUrl(rehearsalId)).header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("RH4093"));
    }

    @Test
    @DisplayName("이미 분석 중인 리허설은 다시 제출할 수 없다")
    void rejectsDuplicateComplete() throws Exception {
        long rehearsalId = analyzingRehearsal();

        mockMvc.perform(post(completeUrl(rehearsalId)).header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("RH4092"));
    }

    @Test
    @DisplayName("실패하지 않은 리허설은 재시도할 수 없다")
    void rejectsRetryWhenNotFailed() throws Exception {
        long rehearsalId = analyzingRehearsal();

        mockMvc.perform(post(retryUrl(rehearsalId)).header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("RH4094"));
    }

    @Test
    @DisplayName("실패한 리허설을 재시도하면 재시도 횟수가 늘고 다시 분석 중이 된다")
    void retriesFailedAnalysis() throws Exception {
        long rehearsalId = analyzingRehearsal();
        willThrow(new BusinessException(ErrorCode.AI_SERVER_ERROR))
                .given(aiClient).call(eq(AiApi.PASSPORT), any(), eq(PassportAnalyzeResponse.class), anyLong());
        passportAnalysisService.analyze(rehearsalId);

        mockMvc.perform(post(retryUrl(rehearsalId)).header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.data.status.code").value("ANALYZING"))
                .andExpect(jsonPath("$.data.retryCount").value(1))
                .andExpect(jsonPath("$.data.failureCode").doesNotExist());
    }

    @Test
    @DisplayName("남의 리허설은 분석을 시작할 수 없다")
    void rejectsOtherUsersRehearsal() throws Exception {
        long rehearsalId = readyRehearsal();
        String otherToken = loginAs("kakao-2", "김하늘");

        mockMvc.perform(post(completeUrl(rehearsalId)).header(HttpHeaders.AUTHORIZATION, bearer(otherToken)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("RH4031"));
    }

    @Test
    @DisplayName("분석 전에는 패스포트를 조회할 수 없다")
    void rejectsPassportBeforeAnalysis() throws Exception {
        mockMvc.perform(get(PASSPORT_URL).header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PP4041"));
    }

    @Test
    @DisplayName("토큰이 없으면 패스포트를 조회할 수 없다")
    void rejectsPassportWithoutToken() throws Exception {
        mockMvc.perform(get(PASSPORT_URL))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("유지 가능액이 0원이어도 패스포트를 저장한다")
    void savesPassportWithZeroSustainableContribution() throws Exception {
        long rehearsalId = analyzingRehearsal();
        PassportAnalyzeResponse response = successResponse();
        given(aiClient.call(eq(AiApi.PASSPORT), any(), eq(PassportAnalyzeResponse.class), anyLong()))
                .willReturn(withSustainable(response, 0L));

        passportAnalysisService.analyze(rehearsalId);

        mockMvc.perform(get(PASSPORT_URL).header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.sustainableMonthlyContribution").value(0));
    }

    private PassportAnalyzeResponse successResponse() throws Exception {
        return objectMapper.readValue(
                objectMapper.readTree(new ClassPathResource("fixtures/penfit-ai-success-responses.json")
                        .getInputStream()).path("pensionPassportAnalyze").traverse(),
                PassportAnalyzeResponse.class);
    }

    private PassportAnalyzeResponse withAnalyses(PassportAnalyzeResponse source,
                                                 List<PassportAnalyzeResponse.DetailedAnalysis> analyses) {
        return new PassportAnalyzeResponse(source.typeCode(), source.typeName(), source.typeSummary(),
                source.sustainableMonthlyContribution(), source.biggestInterruptionRisk(),
                source.marketRiskLevel(), source.analysisSummary(), source.judgmentReason(),
                analyses, source.modelVersion());
    }

    private PassportAnalyzeResponse withSustainable(PassportAnalyzeResponse source, Long sustainable) {
        return new PassportAnalyzeResponse(source.typeCode(), source.typeName(), source.typeSummary(),
                sustainable, source.biggestInterruptionRisk(), source.marketRiskLevel(),
                source.analysisSummary(), source.judgmentReason(),
                source.detailedAnalysis(), source.modelVersion());
    }

    private long analyzingRehearsal() throws Exception {
        long rehearsalId = readyRehearsal();
        mockMvc.perform(post(completeUrl(rehearsalId)).header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
                .andExpect(status().isAccepted());
        return rehearsalId;
    }

    private long readyRehearsal() throws Exception {
        long rehearsalId = startRehearsal();
        for (Map.Entry<String, String> entry : ANSWERS.entrySet()) {
            answer(rehearsalId, entry.getKey(), entry.getValue());
        }
        return rehearsalId;
    }

    private long startRehearsal() throws Exception {
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

    private void answer(long rehearsalId, String scenarioCode, String optionCode) throws Exception {
        mockMvc.perform(post("/api/v1/rehearsals/%d/answers/%s".formatted(rehearsalId, scenarioCode))
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"optionCode\":\"%s\"}".formatted(optionCode)))
                .andExpect(status().isCreated());
    }

    private String completeUrl(long rehearsalId) {
        return "/api/v1/rehearsals/%d/complete".formatted(rehearsalId);
    }

    private String retryUrl(long rehearsalId) {
        return "/api/v1/rehearsals/%d/analysis/retry".formatted(rehearsalId);
    }

    private String rehearsalUrl(long rehearsalId) {
        return "/api/v1/rehearsals/%d".formatted(rehearsalId);
    }

    private String loginAs(String kakaoId, String nickname) throws Exception {
        given(kakaoOAuthClient.fetchUserInfo(anyString()))
                .willReturn(new KakaoUserInfo(kakaoId, nickname));

        String body = mockMvc.perform(post("/api/v1/auth/kakao/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"authorization-code\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(body).path("data").path("accessToken").asText();
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
