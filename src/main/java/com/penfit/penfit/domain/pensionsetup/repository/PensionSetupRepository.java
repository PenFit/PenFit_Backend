package com.penfit.penfit.domain.pensionsetup.repository;

import com.penfit.penfit.domain.pensionsetup.entity.PensionSetup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PensionSetupRepository extends JpaRepository<PensionSetup, Long> {

    Optional<PensionSetup> findByUserId(Long userId);

    boolean existsByUserId(Long userId);
}
