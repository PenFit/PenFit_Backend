package com.penfit.penfit.domain.mission.entity;

import com.penfit.penfit.global.enums.CategoryCode;
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

import java.math.BigDecimal;

@Getter
@Entity
@Table(name = "spending_category_amounts")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SpendingCategoryAmount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "analysis_id", nullable = false)
    private Long analysisId;

    @Enumerated(EnumType.STRING)
    @Column(name = "category_code", nullable = false, length = 30)
    private CategoryCode categoryCode;

    @Column(nullable = false)
    private Long amount;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal ratio;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @Builder
    private SpendingCategoryAmount(Long analysisId, CategoryCode categoryCode, Long amount,
                                   BigDecimal ratio, Integer displayOrder) {
        this.analysisId = analysisId;
        this.categoryCode = categoryCode;
        this.amount = amount;
        this.ratio = ratio;
        this.displayOrder = displayOrder;
    }
}
