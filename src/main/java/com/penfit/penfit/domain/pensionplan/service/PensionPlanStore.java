package com.penfit.penfit.domain.pensionplan.service;

import com.penfit.penfit.domain.financialprofile.entity.FinancialProfile;
import com.penfit.penfit.domain.financialprofile.repository.FinancialProfileRepository;
import com.penfit.penfit.domain.passport.entity.PensionPassport;
import com.penfit.penfit.domain.passport.repository.PensionPassportRepository;
import com.penfit.penfit.domain.pensionplan.dto.PensionPlanResponse;
import com.penfit.penfit.domain.pensionplan.entity.PensionPlan;
import com.penfit.penfit.domain.pensionplan.entity.PensionPlanAdvantage;
import com.penfit.penfit.domain.pensionplan.repository.PensionPlanAdvantageRepository;
import com.penfit.penfit.domain.pensionplan.repository.PensionPlanRepository;
import com.penfit.penfit.domain.pensionsetup.entity.PensionSetup;
import com.penfit.penfit.domain.pensionsetup.repository.PensionSetupRepository;
import com.penfit.penfit.global.calculator.PensionAssetCalculator;
import com.penfit.penfit.global.client.ai.dto.PensionPlanGenerateRequest;
import com.penfit.penfit.global.client.ai.dto.PensionPlanGenerateResponse;
import com.penfit.penfit.global.config.PensionProperties;
import com.penfit.penfit.global.enums.AccountType;
import com.penfit.penfit.global.error.BusinessException;
import com.penfit.penfit.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class PensionPlanStore {

    private final PensionPlanRepository pensionPlanRepository;
    private final PensionPlanAdvantageRepository pensionPlanAdvantageRepository;
    private final FinancialProfileRepository financialProfileRepository;
    private final PensionSetupRepository pensionSetupRepository;
    private final PensionPassportRepository pensionPassportRepository;
    private final PensionAssetCalculator pensionAssetCalculator;
    private final PensionProperties pensionProperties;

    public record PlanContext(Long userId, Long passportId, PensionPlanGenerateRequest request) {
    }

    @Transactional(readOnly = true)
    public PlanContext loadContext(Long userId) {
        if (pensionPlanRepository.existsByUserId(userId)) {
            throw new BusinessException(ErrorCode.PENSION_PLAN_ALREADY_EXISTS);
        }

        FinancialProfile profile = financialProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.FINANCIAL_PROFILE_NOT_FOUND));
        PensionSetup setup = pensionSetupRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PENSION_SETUP_NOT_FOUND));
        PensionPassport passport = pensionPassportRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PASSPORT_NOT_FOUND));

        if (passport.getSustainableMonthlyContribution() == 0) {
            throw new BusinessException(ErrorCode.NO_SUSTAINABLE_CONTRIBUTION);
        }

        return new PlanContext(userId, passport.getId(),
                PensionPlanGenerateRequest.of(profile, setup, passport));
    }

    @Transactional
    public PensionPlanResponse save(PlanContext context, PensionPlanGenerateResponse response,
                                    String rawResponse) {
        long expectedFutureAsset = pensionAssetCalculator.futureValue(
                response.monthlyContribution(),
                pensionProperties.expectedReturnRate(),
                pensionProperties.contributionYears());

        PensionPlan plan;
        try {
            plan = pensionPlanRepository.saveAndFlush(PensionPlan.builder()
                    .userId(context.userId())
                    .passportId(context.passportId())
                    .planName(response.planName())
                    .accountType(AccountType.valueOf(response.accountType()))
                    .monthlyContribution(response.monthlyContribution())
                    .stockRatio(response.assetAllocation().stockRatio())
                    .bondRatio(response.assetAllocation().bondRatio())
                    .depositRatio(response.assetAllocation().depositRatio())
                    .recommendationReason(response.recommendationReason())
                    .expectedFutureAsset(expectedFutureAsset)
                    .contributionYears(pensionProperties.contributionYears())
                    .expectedReturnRate(pensionProperties.expectedReturnRate())
                    .aiRawResponse(rawResponse)
                    .modelVersion(response.modelVersion())
                    .build());
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(ErrorCode.PENSION_PLAN_ALREADY_EXISTS, e);
        }

        List<PensionPlanAdvantage> advantages = pensionPlanAdvantageRepository.saveAll(
                IntStream.range(0, response.advantages().size())
                        .mapToObj(index -> PensionPlanAdvantage.builder()
                                .planId(plan.getId())
                                .displayOrder(index + 1)
                                .content(response.advantages().get(index))
                                .build())
                        .toList());

        return PensionPlanResponse.of(plan, advantages);
    }

    @Transactional(readOnly = true)
    public PensionPlanResponse getMyPlan(Long userId) {
        PensionPlan plan = pensionPlanRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PENSION_PLAN_NOT_FOUND));
        return PensionPlanResponse.of(plan,
                pensionPlanAdvantageRepository.findAllByPlanIdOrderByDisplayOrderAsc(plan.getId()));
    }
}
