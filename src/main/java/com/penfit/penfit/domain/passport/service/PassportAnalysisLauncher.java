package com.penfit.penfit.domain.passport.service;

import com.penfit.penfit.domain.rehearsal.event.RehearsalAnalysisRequested;
import com.penfit.penfit.global.error.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.concurrent.RejectedExecutionException;

@Slf4j
@Component
public class PassportAnalysisLauncher {

    private final ThreadPoolTaskExecutor aiTaskExecutor;
    private final PassportAnalysisService passportAnalysisService;

    public PassportAnalysisLauncher(@Qualifier("aiTaskExecutor") ThreadPoolTaskExecutor aiTaskExecutor,
                                    PassportAnalysisService passportAnalysisService) {
        this.aiTaskExecutor = aiTaskExecutor;
        this.passportAnalysisService = passportAnalysisService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onAnalysisRequested(RehearsalAnalysisRequested event) {
        try {
            aiTaskExecutor.execute(() -> passportAnalysisService.analyze(event.rehearsalId()));
        } catch (RejectedExecutionException e) {
            log.error("AI 분석 대기열이 가득 참 rehearsalId={}", event.rehearsalId());
            passportAnalysisService.markFailed(event.rehearsalId(),
                    ErrorCode.AI_SERVER_ERROR, "분석 요청이 몰려 처리하지 못했어요. 다시 시도해주세요.");
        }
    }
}
