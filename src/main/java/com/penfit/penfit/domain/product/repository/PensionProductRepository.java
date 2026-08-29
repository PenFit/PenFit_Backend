package com.penfit.penfit.domain.product.repository;

import com.penfit.penfit.domain.product.entity.PensionProduct;
import com.penfit.penfit.global.enums.AccountType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PensionProductRepository extends JpaRepository<PensionProduct, Long> {

    Optional<PensionProduct> findByIdAndIsActiveTrue(Long id);

    boolean existsByIdAndIsActiveTrue(Long id);

    List<PensionProduct> findAllByIdInAndIsActiveTrue(Collection<Long> ids);

    List<PensionProduct> findAllByAccountTypeAndIsActiveTrueOrderByIdAsc(AccountType accountType);
}
