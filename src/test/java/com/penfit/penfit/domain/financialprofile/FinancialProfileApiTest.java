package com.penfit.penfit.domain.financialprofile;

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
class FinancialProfileApiTest {

    private static final String URL = "/api/v1/users/me/financial-profile";

    private static final String VALID_BODY = """
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
    @DisplayName("입력 전에 조회하면 FP4041 로 안내한다")
    void returnsNotFoundBeforeRegistration() throws Exception {
        mockMvc.perform(get(URL).header(HttpHeaders.AUTHORIZATION, bearer()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("FP4041"));
    }

    @Test
    @DisplayName("최초 등록하면 201 과 함께 코드·화면 문구를 돌려준다")
    void createsProfile() throws Exception {
        mockMvc.perform(post(URL)
                        .header(HttpHeaders.AUTHORIZATION, bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("CM2011"))
                .andExpect(jsonPath("$.data.ageBand.code").value("AGE_26_28"))
                .andExpect(jsonPath("$.data.ageBand.displayName").value("20대 중반 (26~28세)"))
                .andExpect(jsonPath("$.data.debtBand.displayName").value("없음"))
                .andExpect(jsonPath("$.data.monthlySalary").value(2800000));
    }

    @Test
    @DisplayName("등록 후에는 조회된다")
    void readsAfterRegistration() throws Exception {
        register();

        mockMvc.perform(get(URL).header(HttpHeaders.AUTHORIZATION, bearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.occupationType.code").value("REGULAR_EMPLOYEE"))
                .andExpect(jsonPath("$.data.occupationType.displayName").value("정규직"))
                .andExpect(jsonPath("$.data.currentInvestment").value(100000));
    }

    @Test
    @DisplayName("두 번째 등록은 FP4091 로 막는다")
    void rejectsSecondRegistration() throws Exception {
        register();

        mockMvc.perform(post(URL)
                        .header(HttpHeaders.AUTHORIZATION, bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("FP4091"));
    }

    @Test
    @DisplayName("월급이 음수면 400 으로 막는다")
    void rejectsNegativeSalary() throws Exception {
        mockMvc.perform(post(URL)
                        .header(HttpHeaders.AUTHORIZATION, bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY.replace("2800000", "-1")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("CM4001"))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("monthlySalary")));
    }

    @Test
    @DisplayName("저축금액이 음수면 400 으로 막는다")
    void rejectsNegativeSavings() throws Exception {
        mockMvc.perform(post(URL)
                        .header(HttpHeaders.AUTHORIZATION, bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY.replace("300000", "-5000")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("CM4001"));
    }

    @Test
    @DisplayName("필수 항목이 빠지면 400 으로 막는다")
    void rejectsMissingField() throws Exception {
        mockMvc.perform(post(URL)
                        .header(HttpHeaders.AUTHORIZATION, bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ageBand\":\"AGE_26_28\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("CM4001"));
    }

    @Test
    @DisplayName("허용되지 않은 Enum 코드는 사용 가능한 값을 알려준다")
    void rejectsUnknownEnumWithAllowedValues() throws Exception {
        mockMvc.perform(post(URL)
                        .header(HttpHeaders.AUTHORIZATION, bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY.replace("AGE_26_28", "AGE_99_99")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("CM4001"))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("ageBand")))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("AGE_23_25")));
    }

    @Test
    @DisplayName("생활비가 월급과 같은 경계 입력도 저장은 허용한다")
    void acceptsBoundaryInput() throws Exception {
        mockMvc.perform(post(URL)
                        .header(HttpHeaders.AUTHORIZATION, bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY.replace("\"monthlySavings\": 300000", "\"monthlySavings\": 0")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.monthlySavings").value(0));
    }

    @Test
    @DisplayName("토큰 없이 호출하면 401 을 반환한다")
    void rejectsWithoutToken() throws Exception {
        mockMvc.perform(get(URL))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("CM4011"));
    }

    private void register() throws Exception {
        mockMvc.perform(post(URL)
                        .header(HttpHeaders.AUTHORIZATION, bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isCreated());
    }

    private String bearer() {
        return "Bearer " + accessToken;
    }
}
