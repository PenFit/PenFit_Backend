package com.penfit.penfit.domain.product.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "saved_products")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SavedProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "saved_at", nullable = false)
    private OffsetDateTime savedAt;

    @Builder
    private SavedProduct(Long userId, Long productId) {
        this.userId = userId;
        this.productId = productId;
    }

    @PrePersist
    void onCreate() {
        this.savedAt = OffsetDateTime.now();
    }
}
