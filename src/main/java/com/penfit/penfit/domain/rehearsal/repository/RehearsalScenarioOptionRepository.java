package com.penfit.penfit.domain.rehearsal.repository;

import com.penfit.penfit.domain.rehearsal.entity.RehearsalScenarioOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RehearsalScenarioOptionRepository extends JpaRepository<RehearsalScenarioOption, Long> {

    List<RehearsalScenarioOption> findAllByOrderByScenarioCodeAscDisplayOrderAsc();
}
