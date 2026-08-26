package com.penfit.penfit.domain.pensionsetup.service;

import com.penfit.penfit.domain.pensionsetup.dto.PensionSetupCreateRequest;
import com.penfit.penfit.domain.pensionsetup.dto.PensionSetupResponse;
import com.penfit.penfit.domain.pensionsetup.dto.PensionSetupResponse.GrowthPoint;
import com.penfit.penfit.domain.pensionsetup.entity.PensionSetup;
import com.penfit.penfit.domain.pensionsetup.repository.PensionSetupRepository;
import com.penfit.penfit.global.calculator.PensionAssetCalculator;
import com.penfit.penfit.global.config.PensionProperties;
import com.penfit.penfit.global.error.BusinessException;
import com.penfit.penfit.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PensionSetupService {

    private static final List<Integer> GROWTH_MILESTONES = List.of(10, 15, 20, 25, 30);

    private final PensionSetupRepository pensionSetupRepository;
    private final PensionAssetCalculator pensionAssetCalculator;
    private final PensionProperties pensionProperties;

    @Transactional(readOnly = true)
    public PensionSetupResponse getMySetup(Long userId) {
        PensionSetup setup = pensionSetupRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PENSION_SETUP_NOT_FOUND));
        return PensionSetupResponse.of(setup, growthOf(setup.getMonthlyContribution()));
    }

    @Transactional
    public PensionSetupResponse create(Long userId, PensionSetupCreateRequest request) {
        if (request.monthlyContribution() < pensionProperties.minimumMonthlyContribution()) {
            throw new BusinessException(ErrorCode.INVALID_MONTHLY_CONTRIBUTION);
        }
        if (pensionSetupRepository.existsByUserId(userId)) {
            throw new BusinessException(ErrorCode.PENSION_SETUP_ALREADY_EXISTS);
        }

        long previewFutureAsset = pensionAssetCalculator.futureValue(
                request.monthlyContribution(),
                pensionProperties.expectedReturnRate(),
                pensionProperties.contributionYears());

        PensionSetup saved = pensionSetupRepository.save(PensionSetup.builder()
                .userId(userId)
                .accountType(request.accountType())
                .monthlyContribution(request.monthlyContribution())
                .previewFutureAsset(previewFutureAsset)
                .expectedReturnRate(pensionProperties.expectedReturnRate())
                .contributionYears(pensionProperties.contributionYears())
                .build());

        return PensionSetupResponse.of(saved, growthOf(saved.getMonthlyContribution()));
    }

    private List<GrowthPoint> growthOf(long monthlyContribution) {
        return GROWTH_MILESTONES.stream()
                .map(years -> new GrowthPoint(years, pensionAssetCalculator.futureValue(
                        monthlyContribution, pensionProperties.expectedReturnRate(), years)))
                .toList();
    }
}
