package com.penfit.penfit.domain.mission.repository;

import com.penfit.penfit.domain.mission.entity.SpendingKeyInsight;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpendingKeyInsightRepository extends JpaRepository<SpendingKeyInsight, Long> {

    List<SpendingKeyInsight> findAllByAnalysisIdOrderByDisplayOrderAsc(Long analysisId);
}
