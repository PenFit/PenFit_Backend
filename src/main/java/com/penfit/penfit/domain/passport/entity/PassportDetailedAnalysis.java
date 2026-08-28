package com.penfit.penfit.domain.passport.entity;

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
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "passport_detailed_analyses")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PassportDetailedAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "passport_id", nullable = false)
    private Long passportId;

    @Enumerated(EnumType.STRING)
    @Column(name = "scenario_code", nullable = false, length = 30)
    private ScenarioCode scenarioCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "selected_option_code", nullable = false, length = 50)
    private OptionCode selectedOptionCode;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @Column(name = "behavior_summary", nullable = false)
    private String behaviorSummary;

    @Column(nullable = false)
    private String interpretation;

    @Builder
    private PassportDetailedAnalysis(Long passportId, ScenarioCode scenarioCode, OptionCode selectedOptionCode,
                                     Integer displayOrder, String behaviorSummary, String interpretation) {
        this.passportId = passportId;
        this.scenarioCode = scenarioCode;
        this.selectedOptionCode = selectedOptionCode;
        this.displayOrder = displayOrder;
        this.behaviorSummary = behaviorSummary;
        this.interpretation = interpretation;
    }
}
