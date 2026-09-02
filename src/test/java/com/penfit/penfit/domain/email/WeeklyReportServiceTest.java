package com.penfit.penfit.domain.email;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.penfit.penfit.domain.email.entity.EmailSendLog;
import com.penfit.penfit.domain.email.repository.EmailSendLogRepository;
import com.penfit.penfit.domain.email.service.EmailSender;
import com.penfit.penfit.domain.email.service.WeeklyReportService;
import com.penfit.penfit.domain.mission.entity.BehaviorMission;
import com.penfit.penfit.domain.mission.entity.SpendingAnalysis;
import com.penfit.penfit.domain.mission.entity.SpendingCategoryAmount;
import com.penfit.penfit.domain.mission.repository.BehaviorMissionRepository;
import com.penfit.penfit.domain.mission.repository.SpendingAnalysisRepository;
import com.penfit.penfit.domain.mission.repository.SpendingCategoryAmountRepository;
import com.penfit.penfit.global.client.kakao.KakaoOAuthClient;
import com.penfit.penfit.global.client.kakao.KakaoUserInfo;
import com.penfit.penfit.global.common.ServiceTime;
import com.penfit.penfit.global.enums.CategoryCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
class WeeklyReportServiceTest {

    private static final String EMAIL_URL = "/api/v1/users/me/email";
    private static final String CONSENT_URL = "/api/v1/users/me/email-consent";
    private static final String MY_EMAIL = "jaewon@email.com";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WeeklyReportService weeklyReportService;

    @Autowired
    private EmailSendLogRepository emailSendLogRepository;

    @Autowired
    private SpendingAnalysisRepository spendingAnalysisRepository;

    @Autowired
    private SpendingCategoryAmountRepository spendingCategoryAmountRepository;

    @Autowired
    private BehaviorMissionRepository behaviorMissionRepository;

    @MockitoBean
    private KakaoOAuthClient kakaoOAuthClient;

    @MockitoBean
    private EmailSender emailSender;

    private long userId;
    private String accessToken;

    @BeforeEach
    void setUp() throws Exception {
        accessToken = loginAs("kakao-1", "이재원");
    }

    @Test
    @DisplayName("수신 동의한 사용자에게 보내고 성공 이력을 남긴다")
    void sendsToConsentingUser() throws Exception {
        subscribe();
        long analysisId = saveAnalysis();
        saveMission(analysisId, "커피값 아껴서 연금 넣어보기", false);

        weeklyReportService.sendWeeklyReports();

        List<EmailSendLog> logs = myLogs();
        assertThat(logs).hasSize(1);
        assertThat(logs.get(0).getStatus()).isEqualTo("SUCCESS");
        assertThat(logs.get(0).getEmail()).isEqualTo(MY_EMAIL);
    }

    @Test
    @DisplayName("제목은 주차를 표기하고 본문에 미션과 소비 분석이 담긴다")
    void buildsReportContent() throws Exception {
        subscribe();
        long analysisId = saveAnalysis();
        saveMission(analysisId, "커피값 아껴서 연금 넣어보기", false);

        weeklyReportService.sendWeeklyReports();

        ArgumentCaptor<String> subject = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> html = ArgumentCaptor.forClass(String.class);
        Mockito.verify(emailSender).send(eq(MY_EMAIL), subject.capture(), html.capture());

        LocalDate today = ServiceTime.today();
        assertThat(subject.getValue())
                .isEqualTo("[PenFit] %d월 %d주차 연금 리포트"
                        .formatted(today.getMonthValue(), (today.getDayOfMonth() - 1) / 7 + 1));
        assertThat(html.getValue())
                .contains("이재원님의 연금생활")
                .contains("커피값 아껴서 연금 넣어보기")
                .contains("20,000원")
                .contains("외식·배달")
                .contains("이메일 수신을 거부");
    }

    @Test
    @DisplayName("닉네임에 태그가 있어도 본문에 그대로 심지 않는다")
    void escapesNickname() throws Exception {
        accessToken = loginAs("kakao-9", "<b>해커</b>");
        subscribe();
        long analysisId = saveAnalysis();
        saveMission(analysisId, "커피값 아껴서 연금 넣어보기", false);

        weeklyReportService.sendWeeklyReports();

        ArgumentCaptor<String> html = ArgumentCaptor.forClass(String.class);
        Mockito.verify(emailSender).send(eq(MY_EMAIL), anyString(), html.capture());
        assertThat(html.getValue()).doesNotContain("<b>해커</b>").contains("&lt;b&gt;해커&lt;/b&gt;");
    }

    @Test
    @DisplayName("수신 동의하지 않은 사용자에게는 보내지 않는다")
    void skipsWithoutConsent() throws Exception {
        mockMvc.perform(put(EMAIL_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + MY_EMAIL + "\"}"))
                .andExpect(status().isOk());
        long analysisId = saveAnalysis();
        saveMission(analysisId, "커피값 아껴서 연금 넣어보기", false);

        weeklyReportService.sendWeeklyReports();

        Mockito.verify(emailSender, Mockito.never()).send(eq(MY_EMAIL), anyString(), anyString());
    }

    @Test
    @DisplayName("미션이 없는 사용자에게는 보내지 않는다")
    void skipsWithoutMission() throws Exception {
        subscribe();

        weeklyReportService.sendWeeklyReports();

        Mockito.verify(emailSender, Mockito.never()).send(eq(MY_EMAIL), anyString(), anyString());
        assertThat(myLogs()).isEmpty();
    }

    @Test
    @DisplayName("이번 주에 이미 보냈으면 다시 보내지 않는다")
    void doesNotSendTwiceInSameWeek() throws Exception {
        subscribe();
        long analysisId = saveAnalysis();
        saveMission(analysisId, "커피값 아껴서 연금 넣어보기", false);

        weeklyReportService.sendWeeklyReports();
        weeklyReportService.sendWeeklyReports();

        assertThat(myLogs()).hasSize(1);
    }

    @Test
    @DisplayName("발송에 실패하면 실패 이력을 남긴다")
    void recordsFailure() throws Exception {
        subscribe();
        long analysisId = saveAnalysis();
        saveMission(analysisId, "커피값 아껴서 연금 넣어보기", false);
        willThrow(new IllegalStateException("smtp down"))
                .given(emailSender).send(anyString(), anyString(), anyString());

        weeklyReportService.sendWeeklyReports();

        List<EmailSendLog> logs = myLogs();
        assertThat(logs).hasSize(1);
        assertThat(logs.get(0).getStatus()).isEqualTo("FAILED");
        assertThat(logs.get(0).getErrorMessage()).contains("smtp down");
    }

    @Test
    @DisplayName("지난주 미션이 있으면 달성 결과를 함께 담는다")
    void includesPreviousMission() throws Exception {
        subscribe();
        long analysisId = saveAnalysis();
        BehaviorMission previous = saveMission(analysisId, "배달 한 번 줄이기", true);
        saveMission(analysisId, "커피값 아껴서 연금 넣어보기", false);

        weeklyReportService.sendWeeklyReports();

        ArgumentCaptor<String> html = ArgumentCaptor.forClass(String.class);
        Mockito.verify(emailSender).send(eq(MY_EMAIL), anyString(), html.capture());
        assertThat(html.getValue())
                .contains("지난주 미션 결과")
                .contains(previous.getTitle())
                .contains("72,129,359원");
    }

    @Test
    @DisplayName("미션이 하나뿐이면 지난주 결과를 넣지 않는다")
    void omitsPreviousMission() throws Exception {
        subscribe();
        long analysisId = saveAnalysis();
        saveMission(analysisId, "커피값 아껴서 연금 넣어보기", false);

        weeklyReportService.sendWeeklyReports();

        ArgumentCaptor<String> html = ArgumentCaptor.forClass(String.class);
        Mockito.verify(emailSender).send(eq(MY_EMAIL), anyString(), html.capture());
        assertThat(html.getValue()).doesNotContain("지난주 미션 결과");
    }

    private List<EmailSendLog> myLogs() {
        return emailSendLogRepository.findAll().stream()
                .filter(log -> log.getUserId().equals(userId))
                .toList();
    }

    private void subscribe() throws Exception {
        mockMvc.perform(put(EMAIL_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + MY_EMAIL + "\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(patch(CONSENT_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"emailConsent\":true}"))
                .andExpect(status().isOk());
    }

    private long saveAnalysis() {
        SpendingAnalysis analysis = spendingAnalysisRepository.save(SpendingAnalysis.builder()
                .userId(userId)
                .analysisStartDate(LocalDate.of(2026, 8, 24))
                .analysisEndDate(LocalDate.of(2026, 8, 30))
                .topCategoryCode(CategoryCode.FOOD_DELIVERY)
                .recurringExpense(19_000L)
                .reducibleAmount(20_000L)
                .summary("외식·배달 지출 비중이 가장 높아요.")
                .aiRawResponse("{}")
                .modelVersion("spending-mission-model-1.0")
                .build());

        spendingCategoryAmountRepository.save(SpendingCategoryAmount.builder()
                .analysisId(analysis.getId())
                .categoryCode(CategoryCode.FOOD_DELIVERY)
                .amount(140_000L)
                .ratio(BigDecimal.valueOf(54.47))
                .displayOrder(1)
                .build());

        return analysis.getId();
    }

    private BehaviorMission saveMission(long analysisId, String title, boolean completed) {
        BehaviorMission mission = behaviorMissionRepository.save(BehaviorMission.builder()
                .userId(userId)
                .spendingAnalysisId(analysisId)
                .title(title)
                .description("이번 주 3만원 아껴서 연금계좌 추가 납입")
                .reason("외식·배달 지출이 가장 큰 비중을 차지해요.")
                .targetAmount(20_000L)
                .durationDays(7)
                .dueDate(ServiceTime.today().plusDays(7))
                .modelVersion("spending-mission-model-1.0")
                .build());

        if (completed) {
            mission.complete(72_129_359L);
            behaviorMissionRepository.saveAndFlush(mission);
        }
        return mission;
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
