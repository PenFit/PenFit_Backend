package com.penfit.penfit.domain.email.service;

import com.penfit.penfit.domain.mission.entity.BehaviorMission;
import com.penfit.penfit.domain.mission.entity.SpendingAnalysis;
import com.penfit.penfit.domain.mission.entity.SpendingCategoryAmount;
import com.penfit.penfit.domain.mission.repository.BehaviorMissionRepository;
import com.penfit.penfit.domain.mission.repository.SpendingAnalysisRepository;
import com.penfit.penfit.domain.mission.repository.SpendingCategoryAmountRepository;
import com.penfit.penfit.domain.user.entity.User;
import com.penfit.penfit.global.common.ServiceTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class WeeklyReportContentFactory {

    private final BehaviorMissionRepository behaviorMissionRepository;
    private final SpendingAnalysisRepository spendingAnalysisRepository;
    private final SpendingCategoryAmountRepository spendingCategoryAmountRepository;

    public Optional<WeeklyReport> create(User user) {
        List<BehaviorMission> missions = behaviorMissionRepository.findTop2ByUserIdOrderByIdDesc(user.getId());
        if (missions.isEmpty()) {
            return Optional.empty();
        }

        BehaviorMission current = missions.get(0);
        BehaviorMission previous = missions.size() > 1 ? missions.get(1) : null;

        StringBuilder body = new StringBuilder();
        body.append(greeting(user.getNickname()));
        body.append(currentMissionSection(current));
        if (previous != null) {
            body.append(previousMissionSection(previous));
        }
        spendingAnalysisRepository.findFirstByUserIdOrderByIdDesc(user.getId())
                .ifPresent(analysis -> body.append(spendingSection(analysis)));
        body.append(footer());

        return Optional.of(new WeeklyReport(current.getId(), subject(), wrap(body.toString())));
    }

    private String subject() {
        LocalDate today = ServiceTime.today();
        int weekOfMonth = (today.getDayOfMonth() - 1) / 7 + 1;
        return "[PenFit] %d월 %d주차 연금 리포트".formatted(today.getMonthValue(), weekOfMonth);
    }

    private String greeting(String nickname) {
        return """
                <h2 style="margin:0 0 8px;color:#0f172a;">%s님의 연금생활</h2>
                <p style="margin:0 0 24px;color:#475569;">이번 주 행동 미션과 소비 분석을 정리했어요.</p>
                """.formatted(escape(nickname));
    }

    private String currentMissionSection(BehaviorMission mission) {
        long daysLeft = Math.max(
                ChronoUnit.DAYS.between(ServiceTime.today(), mission.getDueDate()), 0);

        return """
                <div style="border:1px solid #e2e8f0;border-radius:12px;padding:16px;margin-bottom:16px;">
                  <p style="margin:0 0 4px;color:#b45309;font-weight:bold;">이번 주 행동 미션</p>
                  <p style="margin:0 0 12px;font-size:18px;font-weight:bold;color:#0f172a;">%s</p>
                  <p style="margin:0;color:#475569;">목표 %s원 · %s까지 (%d일 남음)</p>
                </div>
                """.formatted(
                escape(mission.getTitle()),
                money(mission.getTargetAmount()),
                mission.getDueDate(),
                daysLeft);
    }

    private String previousMissionSection(BehaviorMission mission) {
        if (mission.isCompleted()) {
            return """
                    <div style="border:1px solid #e2e8f0;border-radius:12px;padding:16px;margin-bottom:16px;">
                      <p style="margin:0 0 4px;color:#047857;font-weight:bold;">지난주 미션 결과</p>
                      <p style="margin:0 0 12px;font-size:18px;font-weight:bold;color:#0f172a;">%s · 달성</p>
                      <p style="margin:0;color:#475569;">확보한 금액 %s원 · 이 금액을 30년간 납입하면 예상 연금자산이 %s원 늘어나요.</p>
                    </div>
                    """.formatted(
                    escape(mission.getTitle()),
                    money(mission.getTargetAmount()),
                    money(mission.getPensionImpactAmount()));
        }

        return """
                <div style="border:1px solid #e2e8f0;border-radius:12px;padding:16px;margin-bottom:16px;">
                  <p style="margin:0 0 4px;color:#64748b;font-weight:bold;">지난주 미션 결과</p>
                  <p style="margin:0 0 12px;font-size:18px;font-weight:bold;color:#0f172a;">%s · 미달성</p>
                  <p style="margin:0;color:#475569;">이번 주에 다시 도전해보세요.</p>
                </div>
                """.formatted(escape(mission.getTitle()));
    }

    private String spendingSection(SpendingAnalysis analysis) {
        String ratio = spendingCategoryAmountRepository
                .findAllByAnalysisIdOrderByDisplayOrderAsc(analysis.getId()).stream()
                .filter(amount -> amount.getCategoryCode() == analysis.getTopCategoryCode())
                .map(SpendingCategoryAmount::getRatio)
                .findFirst()
                .map(value -> value.stripTrailingZeros().toPlainString())
                .orElse("0");

        return """
                <div style="border:1px solid #e2e8f0;border-radius:12px;padding:16px;margin-bottom:16px;">
                  <p style="margin:0 0 4px;color:#1d4ed8;font-weight:bold;">소비 분석</p>
                  <p style="margin:0 0 8px;color:#0f172a;">가장 큰 소비 영역은 %s이고 전체의 %s%%예요.</p>
                  <p style="margin:0;color:#475569;">지금 소비에서 월 %s원을 줄일 여지가 있어요.</p>
                </div>
                """.formatted(
                escape(analysis.getTopCategoryCode().getDisplayName()),
                ratio,
                money(analysis.getReducibleAmount()));
    }

    private String footer() {
        return """
                <p style="margin:24px 0 0;color:#94a3b8;font-size:12px;">
                  이 메일은 수신에 동의하신 분께 매주 보내드려요.
                  더 이상 받고 싶지 않으시면 PenFit 앱의 마이페이지에서 이메일 수신을 거부하시면 됩니다.
                </p>
                """;
    }

    private String wrap(String body) {
        return """
                <div style="font-family:'Apple SD Gothic Neo',sans-serif;max-width:560px;margin:0 auto;padding:24px;">
                %s
                </div>
                """.formatted(body);
    }

    private String money(Long amount) {
        return amount == null ? "0" : String.format("%,d", amount);
    }

    private String escape(String value) {
        return value == null ? "" : HtmlUtils.htmlEscape(value, StandardCharsets.UTF_8.name());
    }
}
