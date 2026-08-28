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
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Getter
@Entity
@Table(name = "rehearsal_answers")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RehearsalAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rehearsal_id", nullable = false)
    private Long rehearsalId;

    @Enumerated(EnumType.STRING)
    @Column(name = "scenario_code", nullable = false, length = 30)
    private ScenarioCode scenarioCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "option_code", nullable = false, length = 50)
    private OptionCode optionCode;

    @Column(name = "answered_at", nullable = false)
    private OffsetDateTime answeredAt;

    @Builder
    private RehearsalAnswer(Long rehearsalId, ScenarioCode scenarioCode, OptionCode optionCode) {
        this.rehearsalId = rehearsalId;
        this.scenarioCode = scenarioCode;
        this.optionCode = optionCode;
    }

    @PrePersist
    void onCreate() {
        this.answeredAt = OffsetDateTime.now();
    }
}
