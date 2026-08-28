package com.penfit.penfit.domain.pensionplan.repository;

import com.penfit.penfit.domain.pensionplan.entity.PensionPlanAdvantage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PensionPlanAdvantageRepository extends JpaRepository<PensionPlanAdvantage, Long> {

    List<PensionPlanAdvantage> findAllByPlanIdOrderByDisplayOrderAsc(Long planId);
}
