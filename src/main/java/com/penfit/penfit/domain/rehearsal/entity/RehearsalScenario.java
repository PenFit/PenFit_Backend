package com.penfit.penfit.domain.rehearsal.entity;

import com.penfit.penfit.global.enums.ScenarioCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;

@Getter
@Entity
@Immutable
@Table(name = "rehearsal_scenarios")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RehearsalScenario {

    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "scenario_code", nullable = false, length = 30)
    private ScenarioCode scenarioCode;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @Column(nullable = false, length = 50)
    private String title;

    @Column(length = 60)
    private String badge;

    @Column(nullable = false)
    private String situation;

    @Column(nullable = false)
    private String question;

    @Column(name = "baseline_contribution")
    private Long baselineContribution;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "context_cards", nullable = false)
    private List<ContextCard> contextCards;

    @Column(name = "irp_notice")
    private String irpNotice;

    public record ContextCard(String label, String value) {
    }
}
