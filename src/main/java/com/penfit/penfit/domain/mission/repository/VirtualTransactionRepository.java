package com.penfit.penfit.domain.mission.repository;

import com.penfit.penfit.domain.mission.entity.VirtualTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VirtualTransactionRepository extends JpaRepository<VirtualTransaction, Long> {

    List<VirtualTransaction> findAllByOrderByTransactedAtAsc();
}
