package com.penfit.penfit.domain.pensionplan.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.penfit.penfit.domain.pensionplan.dto.PensionPlanResponse;
import com.penfit.penfit.domain.pensionplan.service.PensionPlanStore.PlanContext;
import com.penfit.penfit.global.client.ai.AiApi;
import com.penfit.penfit.global.client.ai.AiClient;
import com.penfit.penfit.global.client.ai.dto.PensionPlanGenerateResponse;
import com.penfit.penfit.global.enums.AccountType;
import com.penfit.penfit.global.error.BusinessException;
import com.penfit.penfit.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PensionPlanService {

    private static final int REQUIRED_ADVANTAGE_COUNT = 2;
    private static final BigDecimal RATIO_TOTAL = BigDecimal.valueOf(100);

    private final AiClient aiClient;
    private final PensionPlanStore pensionPlanStore;
    private final ObjectMapper objectMapper;

    public PensionPlanResponse create(Long userId) {
        PlanContext context = pensionPlanStore.loadContext(userId);

        PensionPlanGenerateResponse response = aiClient.call(
                AiApi.PENSION_PLAN, context.request(), PensionPlanGenerateResponse.class, userId);
        validate(response);

        return pensionPlanStore.save(context, response, toJson(response));
    }

    public PensionPlanResponse getMyPlan(Long userId) {
        return pensionPlanStore.getMyPlan(userId);
    }

    private void validate(PensionPlanGenerateResponse response) {
        require(hasText(response.planName()), "planName 이 비어 있다");
        require(hasText(response.recommendationReason()), "recommendationReason 이 비어 있다");
        require(hasText(response.modelVersion()), "modelVersion 이 비어 있다");
        require(isAccountType(response.accountType()), "알 수 없는 accountType: " + response.accountType());
        require(response.monthlyContribution() != null && response.monthlyContribution() > 0,
                "monthlyContribution 은 0보다 커야 한다");

        List<String> advantages = response.advantages();
        require(advantages != null && advantages.size() == REQUIRED_ADVANTAGE_COUNT,
                "advantages 는 %d개여야 한다".formatted(REQUIRED_ADVANTAGE_COUNT));
        advantages.forEach(advantage -> require(hasText(advantage), "advantages 에 빈 문장이 있다"));

        PensionPlanGenerateResponse.AssetAllocation allocation = response.assetAllocation();
        require(allocation != null, "assetAllocation 이 없다");
        require(inRange(allocation.stockRatio()), "stockRatio 가 0~100 범위를 벗어났다");
        require(inRange(allocation.bondRatio()), "bondRatio 가 0~100 범위를 벗어났다");
        require(inRange(allocation.depositRatio()), "depositRatio 가 0~100 범위를 벗어났다");

        BigDecimal total = allocation.stockRatio()
                .add(allocation.bondRatio())
                .add(allocation.depositRatio());
        require(total.compareTo(RATIO_TOTAL) == 0, "자산 비중의 합이 100이 아니다: " + total);
    }

    private boolean inRange(BigDecimal ratio) {
        return ratio != null
                && ratio.compareTo(BigDecimal.ZERO) >= 0
                && ratio.compareTo(RATIO_TOTAL) <= 0;
    }

    private boolean isAccountType(String value) {
        if (value == null) {
            return false;
        }
        try {
            AccountType.valueOf(value);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private void require(boolean condition, String reason) {
        if (!condition) {
            log.warn("연금계획 응답 검증 실패 reason={}", reason);
            throw new BusinessException(ErrorCode.AI_INVALID_RESPONSE);
        }
    }

    private String toJson(PensionPlanGenerateResponse response) {
        try {
            return objectMapper.writeValueAsString(response);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new BusinessException(ErrorCode.AI_INVALID_RESPONSE, e);
        }
    }
}
