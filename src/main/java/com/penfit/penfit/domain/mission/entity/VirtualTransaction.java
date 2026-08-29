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
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import java.time.OffsetDateTime;

@Getter
@Entity
@Immutable
@Table(name = "virtual_transactions")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class VirtualTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "category_code", nullable = false, length = 30)
    private CategoryCode categoryCode;

    @Column(name = "merchant_name", nullable = false, length = 100)
    private String merchantName;

    @Column(nullable = false)
    private Long amount;

    @Column(name = "transacted_at", nullable = false)
    private OffsetDateTime transactedAt;
}
