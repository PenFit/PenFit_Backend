package com.penfit.penfit.domain.passport.repository;

import com.penfit.penfit.domain.passport.entity.PensionPassport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PensionPassportRepository extends JpaRepository<PensionPassport, Long> {

    Optional<PensionPassport> findByUserId(Long userId);
}
