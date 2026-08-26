package com.penfit.penfit.domain.financialprofile.service;

import com.penfit.penfit.domain.financialprofile.dto.FinancialProfileCreateRequest;
import com.penfit.penfit.domain.financialprofile.dto.FinancialProfileResponse;
import com.penfit.penfit.domain.financialprofile.entity.FinancialProfile;
import com.penfit.penfit.domain.financialprofile.repository.FinancialProfileRepository;
import com.penfit.penfit.global.error.BusinessException;
import com.penfit.penfit.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FinancialProfileService {

    private final FinancialProfileRepository financialProfileRepository;

    @Transactional(readOnly = true)
    public FinancialProfileResponse getMyProfile(Long userId) {
        FinancialProfile profile = financialProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.FINANCIAL_PROFILE_NOT_FOUND));
        return FinancialProfileResponse.from(profile);
    }

    @Transactional
    public FinancialProfileResponse create(Long userId, FinancialProfileCreateRequest request) {
        if (financialProfileRepository.existsByUserId(userId)) {
            throw new BusinessException(ErrorCode.FINANCIAL_PROFILE_ALREADY_EXISTS);
        }
        FinancialProfile saved = financialProfileRepository.save(request.toEntity(userId));
        return FinancialProfileResponse.from(saved);
    }
}
