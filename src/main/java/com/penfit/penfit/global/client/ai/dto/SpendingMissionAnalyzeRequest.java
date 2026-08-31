package com.penfit.penfit.global.client.ai.dto;

import com.penfit.penfit.domain.mission.entity.VirtualTransaction;
import com.penfit.penfit.global.common.ServiceTime;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

public record SpendingMissionAnalyzeRequest(
        Long userId,
        LocalDate weekStartDate,
        LocalDate weekEndDate,
        List<TransactionPayload> transactions
) {

    private static final String EXPENSE = "EXPENSE";

    public record TransactionPayload(
            Long transactionId,
            OffsetDateTime transactionDate,
            String type,
            String category,
            String merchantName,
            Long amount
    ) {

        public static TransactionPayload from(VirtualTransaction transaction) {
            return new TransactionPayload(
                    transaction.getId(),
                    transaction.getTransactedAt(),
                    EXPENSE,
                    transaction.getCategoryCode().name(),
                    transaction.getMerchantName(),
                    transaction.getAmount());
        }
    }

    public static SpendingMissionAnalyzeRequest of(Long userId, List<VirtualTransaction> transactions) {
        LocalDate startDate = ServiceTime.toLocalDate(transactions.get(0).getTransactedAt());
        LocalDate endDate = ServiceTime.toLocalDate(
                transactions.get(transactions.size() - 1).getTransactedAt());

        return new SpendingMissionAnalyzeRequest(
                userId,
                startDate,
                endDate,
                transactions.stream().map(TransactionPayload::from).toList());
    }
}
