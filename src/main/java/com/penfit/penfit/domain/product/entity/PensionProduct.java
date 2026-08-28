package com.penfit.penfit.domain.product.entity;

import com.penfit.penfit.global.enums.AccountType;
import com.penfit.penfit.global.enums.ProductType;
import com.penfit.penfit.global.enums.ProviderType;
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

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Entity
@Immutable
@Table(name = "pension_products")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PensionProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider_type", nullable = false, length = 20)
    private ProviderType providerType;

    @Column(name = "provider_name", nullable = false, length = 50)
    private String providerName;

    @Column(name = "product_name", nullable = false, length = 100)
    private String productName;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false, length = 30)
    private AccountType accountType;

    @Enumerated(EnumType.STRING)
    @Column(name = "product_type", nullable = false, length = 30)
    private ProductType productType;

    @Column(nullable = false)
    private String summary;

    @Column(name = "fee_min_rate", nullable = false, precision = 6, scale = 4)
    private BigDecimal feeMinRate;

    @Column(name = "fee_max_rate", nullable = false, precision = 6, scale = 4)
    private BigDecimal feeMaxRate;

    @Column(name = "investment_scope", nullable = false, length = 100)
    private String investmentScope;

    @Column(name = "official_url", nullable = false, length = 500)
    private String officialUrl;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "data_source", nullable = false, length = 100)
    private String dataSource;

    @Column(name = "reference_date", nullable = false)
    private LocalDate referenceDate;
}
