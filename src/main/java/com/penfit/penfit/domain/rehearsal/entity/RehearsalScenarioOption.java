package com.penfit.penfit.domain.rehearsal.entity;

import com.penfit.penfit.global.enums.OptionCode;
import com.penfit.penfit.global.enums.ScenarioCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

@Getter
@Entity
@Immutable
@Table(name = "rehearsal_scenario_options")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RehearsalScenarioOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "scenario_code", nullable = false, length = 30)
    private ScenarioCode scenarioCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "option_code", nullable = false, length = 50)
    private OptionCode optionCode;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @Column(nullable = false)
    private String title;

    @Column
    private String description;
}
