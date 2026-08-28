package com.penfit.penfit.domain.rehearsal.repository;

import com.penfit.penfit.domain.rehearsal.entity.RehearsalAnswer;
import com.penfit.penfit.global.enums.ScenarioCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RehearsalAnswerRepository extends JpaRepository<RehearsalAnswer, Long> {

    List<RehearsalAnswer> findByRehearsalId(Long rehearsalId);

    boolean existsByRehearsalIdAndScenarioCode(Long rehearsalId, ScenarioCode scenarioCode);

    long countByRehearsalId(Long rehearsalId);
}
