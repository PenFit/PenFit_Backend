package com.penfit.penfit.domain.email.scheduler;

import com.penfit.penfit.domain.email.service.WeeklyReportService;
import com.penfit.penfit.global.config.MailProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WeeklyReportScheduler {

    private final WeeklyReportService weeklyReportService;
    private final MailProperties mailProperties;

    @Scheduled(cron = "0 0 9 * * MON", zone = "Asia/Seoul")
    public void sendWeeklyReports() {
        if (!mailProperties.enabled()) {
            log.info("메일 발송이 꺼져 있어 주간 리포트를 건너뜁니다");
            return;
        }
        weeklyReportService.sendWeeklyReports();
    }
}
