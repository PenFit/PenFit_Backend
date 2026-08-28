package com.penfit.penfit.global.client.ai.dto;

import com.penfit.penfit.domain.financialprofile.entity.FinancialProfile;
import com.penfit.penfit.domain.pensionsetup.entity.PensionSetup;
import com.penfit.penfit.domain.rehearsal.entity.RehearsalAnswer;

import java.util.List;

public record PassportAnalyzeRequest(
        FinancialProfilePayload financialProfile,
        PensionSetupPayload pensionSetup,
        List<RehearsalAnswerPayload> rehearsalAnswers
) {

    public record FinancialProfilePayload(
            String ageBand,
            String occupationType,
            Long monthlySalary,
            String livingExpenseBand,
            String assetBand,
            String debtBand,
            String emergencyFundBand,
            Long monthlySavings,
            Long currentInvestment
    ) {
    }

    public record PensionSetupPayload(String accountType, Long monthlyContribution) {
    }

    public record RehearsalAnswerPayload(String scenarioCode, String optionCode) {
    }

    public static PassportAnalyzeRequest of(FinancialProfile profile, PensionSetup setup,
                                            List<RehearsalAnswer> answers) {
        return new PassportAnalyzeRequest(
                new FinancialProfilePayload(
                        profile.getAgeBand().name(),
                        profile.getOccupationType().name(),
                        profile.getMonthlySalary(),
                        profile.getLivingExpenseBand().name(),
                        profile.getAssetBand().name(),
                        profile.getDebtBand().name(),
                        profile.getEmergencyFundBand().name(),
                        profile.getMonthlySavings(),
                        profile.getCurrentInvestment()),
                new PensionSetupPayload(
                        setup.getAccountType().name(),
                        setup.getMonthlyContribution()),
                answers.stream()
                        .map(answer -> new RehearsalAnswerPayload(
                                answer.getScenarioCode().name(),
                                answer.getOptionCode().name()))
                        .toList());
    }
}
