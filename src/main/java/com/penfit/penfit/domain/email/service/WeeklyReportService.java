package com.penfit.penfit.domain.email.service;

import com.penfit.penfit.domain.email.entity.EmailSendLog;
import com.penfit.penfit.domain.email.repository.EmailSendLogRepository;
import com.penfit.penfit.domain.user.entity.User;
import com.penfit.penfit.domain.user.repository.UserRepository;
import com.penfit.penfit.global.common.ServiceTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeeklyReportService {

    private static final String SUCCESS = "SUCCESS";

    private final UserRepository userRepository;
    private final EmailSendLogRepository emailSendLogRepository;
    private final WeeklyReportContentFactory weeklyReportContentFactory;
    private final EmailSender emailSender;

    public int sendWeeklyReports() {
        List<User> targets = userRepository.findAllByEmailConsentTrueOrderByIdAsc();
        int sent = 0;

        for (User user : targets) {
            try {
                if (sendTo(user)) {
                    sent++;
                }
            } catch (RuntimeException e) {
                log.error("주간 리포트 발송 실패 userId={} email={}", user.getId(), mask(user.getEmail()), e);
            }
        }

        log.info("주간 리포트 발송 완료 대상={} 발송={}", targets.size(), sent);
        return sent;
    }

    private boolean sendTo(User user) {
        if (!user.hasEmail()) {
            return false;
        }
        if (alreadySentThisWeek(user.getId())) {
            log.info("이번 주에 이미 보냈습니다 userId={}", user.getId());
            return false;
        }

        Optional<WeeklyReport> report = weeklyReportContentFactory.create(user);
        if (report.isEmpty()) {
            log.info("보낼 미션이 없어 건너뜁니다 userId={}", user.getId());
            return false;
        }

        return deliver(user, report.get());
    }

    private boolean deliver(User user, WeeklyReport report) {
        try {
            trySend(user.getEmail(), report);
            emailSendLogRepository.save(EmailSendLog.success(
                    user.getId(), report.missionId(), user.getEmail(), report.subject()));
            log.info("주간 리포트를 보냈습니다 userId={} email={}", user.getId(), mask(user.getEmail()));
            return true;

        } catch (RuntimeException e) {
            emailSendLogRepository.save(EmailSendLog.failed(
                    user.getId(), report.missionId(), user.getEmail(), report.subject(), e.getMessage()));
            log.warn("주간 리포트 발송에 실패했습니다 userId={} email={}", user.getId(), mask(user.getEmail()), e);
            return false;
        }
    }

    private void trySend(String email, WeeklyReport report) {
        try {
            emailSender.send(email, report.subject(), report.html());
        } catch (RuntimeException first) {
            log.warn("메일 발송을 한 번 더 시도합니다 email={}", mask(email));
            emailSender.send(email, report.subject(), report.html());
        }
    }

    private boolean alreadySentThisWeek(Long userId) {
        LocalDate monday = ServiceTime.today().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        OffsetDateTime from = monday.atStartOfDay(ServiceTime.ZONE).toOffsetDateTime();
        OffsetDateTime to = monday.plusWeeks(1).atStartOfDay(ServiceTime.ZONE).toOffsetDateTime();
        return emailSendLogRepository.existsByUserIdAndStatusAndSentAtBetween(userId, SUCCESS, from, to);
    }

    private String mask(String email) {
        if (email == null || email.isBlank()) {
            return "(없음)";
        }
        int at = email.indexOf('@');
        if (at <= 1) {
            return "***" + email.substring(Math.max(at, 0));
        }
        return email.charAt(0) + "***" + email.substring(at);
    }
}
