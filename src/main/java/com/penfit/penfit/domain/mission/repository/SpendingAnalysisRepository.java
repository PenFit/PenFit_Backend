package com.penfit.penfit.domain.mission.repository;

import com.penfit.penfit.domain.mission.entity.SpendingAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpendingAnalysisRepository extends JpaRepository<SpendingAnalysis, Long> {

    Optional<SpendingAnalysis> findFirstByUserIdOrderByIdDesc(Long userId);
}
