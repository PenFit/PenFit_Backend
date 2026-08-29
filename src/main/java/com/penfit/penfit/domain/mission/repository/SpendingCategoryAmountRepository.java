package com.penfit.penfit.domain.mission.repository;

import com.penfit.penfit.domain.mission.entity.SpendingCategoryAmount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpendingCategoryAmountRepository extends JpaRepository<SpendingCategoryAmount, Long> {

    List<SpendingCategoryAmount> findAllByAnalysisIdOrderByDisplayOrderAsc(Long analysisId);
}
