package com.penfit.penfit.domain.passport.service;

import com.penfit.penfit.domain.passport.dto.PassportResponse;
import com.penfit.penfit.domain.passport.entity.PensionPassport;
import com.penfit.penfit.domain.passport.repository.PassportDetailedAnalysisRepository;
import com.penfit.penfit.domain.passport.repository.PensionPassportRepository;
import com.penfit.penfit.global.error.BusinessException;
import com.penfit.penfit.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PassportService {

    private final PensionPassportRepository pensionPassportRepository;
    private final PassportDetailedAnalysisRepository passportDetailedAnalysisRepository;

    @Transactional(readOnly = true)
    public PassportResponse getMyPassport(Long userId) {
        PensionPassport passport = pensionPassportRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PASSPORT_NOT_FOUND));
        return PassportResponse.of(passport,
                passportDetailedAnalysisRepository.findAllByPassportIdOrderByDisplayOrderAsc(passport.getId()));
    }
}
