package com.penfit.penfit.domain.passport.service;

import com.penfit.penfit.domain.financialprofile.entity.FinancialProfile;
import com.penfit.penfit.domain.financialprofile.repository.FinancialProfileRepository;
import com.penfit.penfit.domain.passport.entity.PensionPassport;
import com.penfit.penfit.domain.passport.repository.PensionPassportRepository;
import com.penfit.penfit.domain.pensionsetup.entity.PensionSetup;
import com.penfit.penfit.domain.pensionsetup.repository.PensionSetupRepository;
import com.penfit.penfit.domain.rehearsal.entity.Rehearsal;
import com.penfit.penfit.domain.rehearsal.entity.RehearsalAnswer;
import com.penfit.penfit.domain.rehearsal.repository.RehearsalAnswerRepository;
import com.penfit.penfit.domain.rehearsal.repository.RehearsalRepository;
import com.penfit.penfit.global.client.ai.dto.PassportAnalyzeRequest;
import com.penfit.penfit.global.client.ai.dto.PassportAnalyzeResponse;
import com.penfit.penfit.global.enums.MarketRiskLevel;
import com.penfit.penfit.global.enums.PassportTypeCode;
import com.penfit.penfit.global.enums.ScenarioCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PassportAnalysisStore {

    private final RehearsalRepository rehearsalRepository;
    private final RehearsalAnswerRepository rehearsalAnswerRepository;
    private final FinancialProfileRepository financialProfileRepository;
    private final PensionSetupRepository pensionSetupRepository;
    private final PensionPassportRepository pensionPassportRepository;

    public record AnalysisContext(Long rehearsalId, Long userId, PassportAnalyzeRequest request) {
    }

    @Transactional(readOnly = true)
    public AnalysisContext loadContext(Long rehearsalId) {
        Rehearsal rehearsal = rehearsalRepository.findById(rehearsalId).orElse(null);
        if (rehearsal == null || !rehearsal.isAnalyzing()) {
            return null;
        }

        FinancialProfile profile = financialProfileRepository.findByUserId(rehearsal.getUserId()).orElse(null);
        PensionSetup setup = pensionSetupRepository.findByUserId(rehearsal.getUserId()).orElse(null);
        if (profile == null || setup == null) {
            return null;
        }

        List<RehearsalAnswer> answers = rehearsalAnswerRepository.findByRehearsalId(rehearsalId).stream()
                .sorted((left, right) -> Integer.compare(
                        left.getScenarioCode().getDisplayOrder(),
                        right.getScenarioCode().getDisplayOrder()))
                .toList();

        return new AnalysisContext(rehearsalId, rehearsal.getUserId(),
                PassportAnalyzeRequest.of(profile, setup, answers));
    }

    @Transactional
    public void savePassport(AnalysisContext context, PassportAnalyzeResponse response, String rawResponse) {
        Rehearsal rehearsal = rehearsalRepository.findById(context.rehearsalId()).orElseThrow();

        pensionPassportRepository.findByUserId(context.userId())
                .ifPresent(existing -> {
                    pensionPassportRepository.delete(existing);
                    pensionPassportRepository.flush();
                });

        PassportTypeCode typeCode = PassportTypeCode.valueOf(response.typeCode());

        pensionPassportRepository.save(PensionPassport.builder()
                .userId(context.userId())
                .rehearsalId(context.rehearsalId())
                .typeCode(typeCode)
                .sustainableMonthlyContribution(response.sustainableMonthlyContribution())
                .biggestInterruptionRiskCode(ScenarioCode.valueOf(response.biggestInterruptionRisk()))
                .marketRiskLevel(MarketRiskLevel.valueOf(response.marketRiskLevel().code()))
                .typeSummary(typeCode.getDescription())
                .summary(response.analysisSummary())
                .judgmentReason(response.judgmentReason())
                .detailedAnalysisReport(response.detailedAnalysisReport())
                .aiRawResponse(rawResponse)
                .modelVersion(response.modelVersion())
                .build());

        rehearsal.completeAnalysis();
    }

    @Transactional
    public void markFailed(Long rehearsalId, String failureCode, String failureMessage) {
        rehearsalRepository.findById(rehearsalId)
                .ifPresent(rehearsal -> rehearsal.failAnalysis(failureCode, failureMessage));
    }
}
