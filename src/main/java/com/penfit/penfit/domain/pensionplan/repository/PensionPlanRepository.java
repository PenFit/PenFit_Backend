package com.penfit.penfit.domain.pensionplan.repository;

import com.penfit.penfit.domain.pensionplan.entity.PensionPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PensionPlanRepository extends JpaRepository<PensionPlan, Long> {

    Optional<PensionPlan> findByUserId(Long userId);

    boolean existsByUserId(Long userId);
}
