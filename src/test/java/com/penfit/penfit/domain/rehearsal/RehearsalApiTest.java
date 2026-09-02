package com.penfit.penfit.domain.rehearsal;

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
class RehearsalApiTest {

    private static final String START_URL = "/api/v1/users/me/rehearsals";
    private static final String PROFILE_URL = "/api/v1/users/me/financial-profile";
    private static final String SETUP_URL = "/api/v1/users/me/pension-setup";

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

    @MockitoBean
    private KakaoOAuthClient kakaoOAuthClient;

    private String accessToken;

    @BeforeEach
    void setUp() throws Exception {
        accessToken = loginAs("kakao-1", "이재원");
    }

    @Test
    @DisplayName("금융정보가 없으면 리허설을 시작할 수 없다")
    void requiresFinancialProfile() throws Exception {
        mockMvc.perform(post(START_URL).header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("FP4041"));
    }

    @Test
    @DisplayName("가상 연금 설정이 없으면 리허설을 시작할 수 없다")
    void requiresPensionSetup() throws Exception {
        registerProfile(accessToken);

        mockMvc.perform(post(START_URL).header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PS4041"));
    }

    @Test
    @DisplayName("리허설을 시작하면 진행 중 상태와 예상 연금자산을 반환한다")
    void startsRehearsal() throws Exception {
        prepare(accessToken, "PENSION_SAVINGS_FUND");

        mockMvc.perform(post(START_URL).header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status.code").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.data.status.displayName").value("진행 중"))
                .andExpect(jsonPath("$.data.previewFutureAsset").value(83_225_864L))
                .andExpect(jsonPath("$.data.totalScenarios").value(6));
    }

    @Test
    @DisplayName("상황 6개를 정해진 순서로 반환한다")
    void returnsSixScenariosInOrder() throws Exception {
        prepare(accessToken, "PENSION_SAVINGS_FUND");
        long rehearsalId = startRehearsal(accessToken);

        mockMvc.perform(get(scenariosUrl(rehearsalId)).header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(6))
                .andExpect(jsonPath("$.data[0].scenarioCode").value("JOB_CHANGE"))
                .andExpect(jsonPath("$.data[0].title").value("이직"))
                .andExpect(jsonPath("$.data[0].badge").value("현재 29세 · 퇴사 후 1개월차"))
                .andExpect(jsonPath("$.data[0].baselineContribution").value(100000))
                .andExpect(jsonPath("$.data[5].scenarioCode").value("MARKET_DOWNTURN"));
    }

    @Test
    @DisplayName("시장 하락만 선택지가 6개이고 나머지는 5개다")
    void marketDownturnHasSixOptions() throws Exception {
        prepare(accessToken, "PENSION_SAVINGS_FUND");
        long rehearsalId = startRehearsal(accessToken);

        mockMvc.perform(get(scenariosUrl(rehearsalId)).header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
                .andExpect(jsonPath("$.data[0].options.length()").value(5))
                .andExpect(jsonPath("$.data[5].options.length()").value(6))
                .andExpect(jsonPath("$.data[0].options[0].label").value("A"))
                .andExpect(jsonPath("$.data[5].options[5].label").value("F"));
    }

    @Test
    @DisplayName("상단 요약 카드를 함께 내려준다")
    void includesContextCards() throws Exception {
        prepare(accessToken, "PENSION_SAVINGS_FUND");
        long rehearsalId = startRehearsal(accessToken);

        mockMvc.perform(get(scenariosUrl(rehearsalId)).header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
                .andExpect(jsonPath("$.data[0].contextCards.length()").value(2))
                .andExpect(jsonPath("$.data[0].contextCards[0].label").value("현재 월 납입액"))
                .andExpect(jsonPath("$.data[0].contextCards[0].value").value("10만원"));
    }

    @Test
    @DisplayName("IRP 사용자에게만 중도인출 안내를 노출한다")
    void showsIrpNoticeOnlyForIrpUser() throws Exception {
        prepare(accessToken, "INDIVIDUAL_IRP");
        long rehearsalId = startRehearsal(accessToken);

        mockMvc.perform(get(scenariosUrl(rehearsalId)).header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
                .andExpect(jsonPath("$.data[3].scenarioCode").value("HOME_PURCHASE"))
                .andExpect(jsonPath("$.data[3].notice").value(
                        org.hamcrest.Matchers.containsString("중도인출")));
    }

    @Test
    @DisplayName("IRP 가 아닌 사용자에게는 안내를 내리지 않는다")
    void hidesIrpNoticeForOtherAccounts() throws Exception {
        prepare(accessToken, "PENSION_SAVINGS_FUND");
        long rehearsalId = startRehearsal(accessToken);

        mockMvc.perform(get(scenariosUrl(rehearsalId)).header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
                .andExpect(jsonPath("$.data[3].notice").doesNotExist());
    }

    @Test
    @DisplayName("답변을 저장하면 진행 상황을 함께 반환한다")
    void savesAnswer() throws Exception {
        prepare(accessToken, "PENSION_SAVINGS_FUND");
        long rehearsalId = startRehearsal(accessToken);

        mockMvc.perform(post(answerUrl(rehearsalId, "JOB_CHANGE"))
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"optionCode\":\"REDUCE_HALF\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.answeredCount").value(1))
                .andExpect(jsonPath("$.data.totalScenarios").value(6))
                .andExpect(jsonPath("$.data.readyToComplete").value(false))
                .andExpect(jsonPath("$.data.answers[0].scenarioCode").value("JOB_CHANGE"))
                .andExpect(jsonPath("$.data.answers[0].optionCode").value("REDUCE_HALF"));
    }

    @Test
    @DisplayName("같은 상황에 두 번 답하면 RH4091 로 막는다")
    void rejectsDuplicateAnswer() throws Exception {
        prepare(accessToken, "PENSION_SAVINGS_FUND");
        long rehearsalId = startRehearsal(accessToken);
        answer(rehearsalId, "JOB_CHANGE", "REDUCE_HALF");

        mockMvc.perform(post(answerUrl(rehearsalId, "JOB_CHANGE"))
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"optionCode\":\"KEEP\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("RH4091"));
    }

    @Test
    @DisplayName("허용되지 않은 상황과 선택지 조합은 RH4001 로 막는다")
    void rejectsInvalidCombination() throws Exception {
        prepare(accessToken, "PENSION_SAVINGS_FUND");
        long rehearsalId = startRehearsal(accessToken);

        mockMvc.perform(post(answerUrl(rehearsalId, "JOB_CHANGE"))
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"optionCode\":\"REBALANCE\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("RH4001"));
    }

    @Test
    @DisplayName("6개를 모두 답하면 완료 가능 상태가 된다")
    void marksReadyWhenAllAnswered() throws Exception {
        prepare(accessToken, "PENSION_SAVINGS_FUND");
        long rehearsalId = startRehearsal(accessToken);

        answer(rehearsalId, "JOB_CHANGE", "REDUCE_HALF");
        answer(rehearsalId, "INDEPENDENCE", "CUT_EXPENSE_AND_KEEP");
        answer(rehearsalId, "MARRIAGE", "REDUCE_CONTRIBUTION");
        answer(rehearsalId, "HOME_PURCHASE", "PAUSE_UNTIL_EVENT");
        answer(rehearsalId, "CHILDBIRTH", "REDUCE_TO_MINIMUM");
        answer(rehearsalId, "MARKET_DOWNTURN", "KEEP");

        mockMvc.perform(get(detailUrl(rehearsalId)).header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.answeredCount").value(6))
                .andExpect(jsonPath("$.data.readyToComplete").value(true))
                .andExpect(jsonPath("$.data.answers.length()").value(6))
                .andExpect(jsonPath("$.data.answers[0].scenarioCode").value("JOB_CHANGE"))
                .andExpect(jsonPath("$.data.answers[5].scenarioCode").value("MARKET_DOWNTURN"));
    }

    @Test
    @DisplayName("남의 리허설에는 접근할 수 없다")
    void rejectsOtherUsersRehearsal() throws Exception {
        prepare(accessToken, "PENSION_SAVINGS_FUND");
        long rehearsalId = startRehearsal(accessToken);

        String otherToken = loginAs("kakao-2", "다른사람");

        mockMvc.perform(get(detailUrl(rehearsalId)).header(HttpHeaders.AUTHORIZATION, bearer(otherToken)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("RH4031"));
    }

    @Test
    @DisplayName("없는 리허설은 RH4041 을 반환한다")
    void rejectsUnknownRehearsal() throws Exception {
        mockMvc.perform(get(detailUrl(999_999L)).header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RH4041"));
    }

    @Test
    @DisplayName("토큰 없이 호출하면 401 을 반환한다")
    void rejectsWithoutToken() throws Exception {
        mockMvc.perform(post(START_URL))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("CM4011"));
    }

    private String loginAs(String kakaoId, String nickname) throws Exception {
        given(kakaoOAuthClient.fetchUserInfo(anyString(), any()))
                .willReturn(new KakaoUserInfo(kakaoId, nickname));

        String body = mockMvc.perform(post("/api/v1/auth/kakao/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"authorization-code\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(body).path("data").path("accessToken").asText();
    }

    private void prepare(String token, String accountType) throws Exception {
        registerProfile(token);
        mockMvc.perform(post(SETUP_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"accountType\":\"%s\",\"monthlyContribution\":100000}".formatted(accountType)))
                .andExpect(status().isCreated());
    }

    private void registerProfile(String token) throws Exception {
        mockMvc.perform(post(PROFILE_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(PROFILE_BODY))
                .andExpect(status().isCreated());
    }

    private long startRehearsal(String token) throws Exception {
        String body = mockMvc.perform(post(START_URL).header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).path("data").path("rehearsalId").asLong();
    }

    private void answer(long rehearsalId, String scenarioCode, String optionCode) throws Exception {
        mockMvc.perform(post(answerUrl(rehearsalId, scenarioCode))
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"optionCode\":\"%s\"}".formatted(optionCode)))
                .andExpect(status().isCreated());
    }

    private String scenariosUrl(long rehearsalId) {
        return "/api/v1/rehearsals/%d/scenarios".formatted(rehearsalId);
    }

    private String answerUrl(long rehearsalId, String scenarioCode) {
        return "/api/v1/rehearsals/%d/answers/%s".formatted(rehearsalId, scenarioCode);
    }

    private String detailUrl(long rehearsalId) {
        return "/api/v1/rehearsals/%d".formatted(rehearsalId);
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
