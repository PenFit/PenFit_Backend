package com.penfit.penfit.domain.financialprofile.repository;

import com.penfit.penfit.domain.financialprofile.entity.FinancialProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FinancialProfileRepository extends JpaRepository<FinancialProfile, Long> {

    Optional<FinancialProfile> findByUserId(Long userId);

    boolean existsByUserId(Long userId);
}
