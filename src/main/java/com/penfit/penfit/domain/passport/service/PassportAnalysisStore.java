package com.penfit.penfit.domain.passport.service;

import com.penfit.penfit.domain.financialprofile.entity.FinancialProfile;
import com.penfit.penfit.domain.financialprofile.repository.FinancialProfileRepository;
import com.penfit.penfit.domain.passport.entity.PassportDetailedAnalysis;
import com.penfit.penfit.domain.passport.entity.PensionPassport;
import com.penfit.penfit.domain.passport.repository.PassportDetailedAnalysisRepository;
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
import com.penfit.penfit.global.enums.OptionCode;
import com.penfit.penfit.global.enums.PassportTypeCode;
import com.penfit.penfit.global.enums.ScenarioCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PassportAnalysisStore {

    private final RehearsalRepository rehearsalRepository;
    private final RehearsalAnswerRepository rehearsalAnswerRepository;
    private final FinancialProfileRepository financialProfileRepository;
    private final PensionSetupRepository pensionSetupRepository;
    private final PensionPassportRepository pensionPassportRepository;
    private final PassportDetailedAnalysisRepository passportDetailedAnalysisRepository;

    public record AnalysisContext(
            Long rehearsalId,
            Long userId,
            PassportAnalyzeRequest request,
            Map<ScenarioCode, OptionCode> answers
    ) {
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

        return new AnalysisContext(
                rehearsalId,
                rehearsal.getUserId(),
                PassportAnalyzeRequest.of(profile, setup, answers),
                answers.stream().collect(Collectors.toMap(
                        RehearsalAnswer::getScenarioCode, RehearsalAnswer::getOptionCode)));
    }

    @Transactional
    public void savePassport(AnalysisContext context, PassportAnalyzeResponse response, String rawResponse) {
        Rehearsal rehearsal = rehearsalRepository.findById(context.rehearsalId()).orElseThrow();

        pensionPassportRepository.findByUserId(context.userId())
                .ifPresent(existing -> {
                    passportDetailedAnalysisRepository.deleteAll(
                            passportDetailedAnalysisRepository
                                    .findAllByPassportIdOrderByDisplayOrderAsc(existing.getId()));
                    pensionPassportRepository.delete(existing);
                    pensionPassportRepository.flush();
                });

        PensionPassport passport = pensionPassportRepository.save(PensionPassport.builder()
                .userId(context.userId())
                .rehearsalId(context.rehearsalId())
                .typeCode(PassportTypeCode.valueOf(response.typeCode()))
                .sustainableMonthlyContribution(response.sustainableMonthlyContribution())
                .biggestInterruptionRiskCode(ScenarioCode.valueOf(response.biggestInterruptionRisk().scenarioCode()))
                .marketRiskLevel(MarketRiskLevel.valueOf(response.marketRiskLevel().code()))
                .typeSummary(response.typeSummary())
                .summary(response.analysisSummary())
                .judgmentReason(response.judgmentReason())
                .aiRawResponse(rawResponse)
                .modelVersion(response.modelVersion())
                .build());

        passportDetailedAnalysisRepository.saveAll(response.detailedAnalysis().stream()
                .map(analysis -> {
                    ScenarioCode scenarioCode = ScenarioCode.valueOf(analysis.scenarioCode());
                    return PassportDetailedAnalysis.builder()
                            .passportId(passport.getId())
                            .scenarioCode(scenarioCode)
                            .selectedOptionCode(context.answers().get(scenarioCode))
                            .displayOrder(scenarioCode.getDisplayOrder())
                            .behaviorSummary(analysis.behaviorSummary())
                            .interpretation(analysis.interpretation())
                            .build();
                })
                .sorted(Comparator.comparing(PassportDetailedAnalysis::getDisplayOrder))
                .toList());

        rehearsal.completeAnalysis();
    }

    @Transactional
    public void markFailed(Long rehearsalId, String failureCode, String failureMessage) {
        rehearsalRepository.findById(rehearsalId)
                .ifPresent(rehearsal -> rehearsal.failAnalysis(failureCode, failureMessage));
    }
}
