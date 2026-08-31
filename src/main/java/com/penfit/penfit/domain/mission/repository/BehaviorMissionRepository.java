package com.penfit.penfit.domain.mission.repository;

import com.penfit.penfit.domain.mission.entity.BehaviorMission;
import com.penfit.penfit.global.enums.MissionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface BehaviorMissionRepository extends JpaRepository<BehaviorMission, Long> {

    Optional<BehaviorMission> findFirstByUserIdOrderByIdDesc(Long userId);

    List<BehaviorMission> findTop2ByUserIdOrderByIdDesc(Long userId);

    List<BehaviorMission> findAllByUserIdAndStatusAndCompletedAtBetweenOrderByCompletedAtDesc(
            Long userId, MissionStatus status, OffsetDateTime from, OffsetDateTime to);
}
