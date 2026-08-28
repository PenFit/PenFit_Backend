package com.penfit.penfit.domain.passport.repository;

import com.penfit.penfit.domain.passport.entity.PassportDetailedAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PassportDetailedAnalysisRepository extends JpaRepository<PassportDetailedAnalysis, Long> {

    List<PassportDetailedAnalysis> findAllByPassportIdOrderByDisplayOrderAsc(Long passportId);
}
