package com.penfit.penfit.domain.product.entity;

import com.penfit.penfit.global.enums.FitLevel;
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

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Entity
@Table(name = "product_recommendations")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductRecommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "plan_id", nullable = false)
    private Long planId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "rank", nullable = false)
    private Integer rank;

    @Column(name = "fit_score", nullable = false, precision = 6, scale = 4)
    private BigDecimal fitScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "fit_level", nullable = false, length = 20)
    private FitLevel fitLevel;

    @Column(name = "recommendation_reason", nullable = false)
    private String recommendationReason;

    @Column(name = "model_version", nullable = false, length = 50)
    private String modelVersion;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Builder
    private ProductRecommendation(Long userId, Long planId, Long productId, Integer rank,
                                  BigDecimal fitScore, FitLevel fitLevel, String recommendationReason,
                                  String modelVersion) {
        this.userId = userId;
        this.planId = planId;
        this.productId = productId;
        this.rank = rank;
        this.fitScore = fitScore;
        this.fitLevel = fitLevel;
        this.recommendationReason = recommendationReason;
        this.modelVersion = modelVersion;
    }

    @PrePersist
    void onCreate() {
        this.createdAt = OffsetDateTime.now();
    }
}
