package com.penfit.penfit.domain.mission.entity;

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
@Table(name = "spending_key_insights")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SpendingKeyInsight {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "analysis_id", nullable = false)
    private Long analysisId;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @Column(nullable = false)
    private String content;

    @Builder
    private SpendingKeyInsight(Long analysisId, Integer displayOrder, String content) {
        this.analysisId = analysisId;
        this.displayOrder = displayOrder;
        this.content = content;
    }
}
