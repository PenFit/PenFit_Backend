package com.penfit.penfit.domain.pensionplan.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "pension_plan_advantages")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PensionPlanAdvantage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "plan_id", nullable = false)
    private Long planId;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @Column(nullable = false)
    private String content;

    @Builder
    private PensionPlanAdvantage(Long planId, Integer displayOrder, String content) {
        this.planId = planId;
        this.displayOrder = displayOrder;
        this.content = content;
    }
}
