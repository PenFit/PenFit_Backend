package com.penfit.penfit.global.client.ai.dto;

import com.penfit.penfit.domain.mission.entity.VirtualTransaction;
import com.penfit.penfit.domain.pensionplan.entity.PensionPlan;
import com.penfit.penfit.global.common.ServiceTime;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

public record SpendingMissionAnalyzeRequest(
        AnalysisPeriod analysisPeriod,
        List<TransactionPayload> transactions,
        PensionPlanPayload pensionPlan,
        MissionCondition missionCondition
) {

    public record AnalysisPeriod(LocalDate startDate, LocalDate endDate) {
    }

    public record TransactionPayload(
            String categoryCode,
            String merchantName,
            Long amount,
            OffsetDateTime transactedAt
    ) {
    }

    public record PensionPlanPayload(Long monthlyContribution, String accountType) {
    }

    public record MissionCondition(int durationDays) {
    }

    public static SpendingMissionAnalyzeRequest of(List<VirtualTransaction> transactions,
                                                   PensionPlan plan, int durationDays) {
        LocalDate startDate = ServiceTime.toLocalDate(transactions.get(0).getTransactedAt());
        LocalDate endDate = ServiceTime.toLocalDate(
                transactions.get(transactions.size() - 1).getTransactedAt());

        return new SpendingMissionAnalyzeRequest(
                new AnalysisPeriod(startDate, endDate),
                transactions.stream()
                        .map(transaction -> new TransactionPayload(
                                transaction.getCategoryCode().name(),
                                transaction.getMerchantName(),
                                transaction.getAmount(),
                                transaction.getTransactedAt()))
                        .toList(),
                new PensionPlanPayload(plan.getMonthlyContribution(), plan.getAccountType().name()),
                new MissionCondition(durationDays));
    }
}
