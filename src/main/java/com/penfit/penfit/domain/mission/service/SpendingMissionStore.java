package com.penfit.penfit.domain.mission.service;

import com.penfit.penfit.domain.mission.dto.BehaviorMissionResponse;
import com.penfit.penfit.domain.mission.dto.MissionCompletionResponse;
import com.penfit.penfit.domain.mission.dto.SpendingAnalysisResponse;
import com.penfit.penfit.domain.mission.dto.SpendingAnalysisResponse.CategorySpendingResponse;
import com.penfit.penfit.domain.mission.entity.BehaviorMission;
import com.penfit.penfit.domain.mission.entity.SpendingAnalysis;
import com.penfit.penfit.domain.mission.entity.SpendingCategoryAmount;
import com.penfit.penfit.domain.mission.entity.VirtualTransaction;
import com.penfit.penfit.domain.mission.repository.BehaviorMissionRepository;
import com.penfit.penfit.domain.mission.repository.SpendingAnalysisRepository;
import com.penfit.penfit.domain.mission.repository.SpendingCategoryAmountRepository;
import com.penfit.penfit.domain.mission.repository.SpendingKeyInsightRepository;
import com.penfit.penfit.domain.mission.repository.VirtualTransactionRepository;
import com.penfit.penfit.domain.mission.entity.SpendingKeyInsight;
import com.penfit.penfit.domain.pensionplan.repository.PensionPlanRepository;
import com.penfit.penfit.global.calculator.PensionAssetCalculator;
import com.penfit.penfit.global.client.ai.dto.SpendingMissionAnalyzeRequest;
import com.penfit.penfit.global.client.ai.dto.SpendingMissionAnalyzeResponse;
import com.penfit.penfit.global.common.CodeName;
import com.penfit.penfit.global.common.ServiceTime;
import com.penfit.penfit.global.config.PensionProperties;
import com.penfit.penfit.global.enums.CategoryCode;
import com.penfit.penfit.global.enums.MissionStatus;
import com.penfit.penfit.global.error.BusinessException;
import com.penfit.penfit.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class SpendingMissionStore {

    private static final int ANALYSIS_DAYS = 7;

    private final VirtualTransactionRepository virtualTransactionRepository;
    private final SpendingAnalysisRepository spendingAnalysisRepository;
    private final SpendingCategoryAmountRepository spendingCategoryAmountRepository;
    private final SpendingKeyInsightRepository spendingKeyInsightRepository;
    private final BehaviorMissionRepository behaviorMissionRepository;
    private final PensionPlanRepository pensionPlanRepository;
    private final PensionAssetCalculator pensionAssetCalculator;
    private final PensionProperties pensionProperties;

    public record AnalysisContext(Long userId, SpendingMissionAnalyzeRequest request) {
    }

    @Transactional(readOnly = true)
    public AnalysisContext loadContext(Long userId) {
        pensionPlanRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PENSION_PLAN_NOT_FOUND));

        List<VirtualTransaction> recent = recentTransactions();
        if (recent.isEmpty()) {
            throw new BusinessException(ErrorCode.NO_ACTIONABLE_SPENDING);
        }

        return new AnalysisContext(userId, SpendingMissionAnalyzeRequest.of(userId, recent));
    }

    private List<VirtualTransaction> recentTransactions() {
        List<VirtualTransaction> all = virtualTransactionRepository.findAllByOrderByTransactedAtAsc();
        if (all.isEmpty()) {
            return all;
        }

        LocalDate lastDate = ServiceTime.toLocalDate(all.get(all.size() - 1).getTransactedAt());
        LocalDate from = lastDate.minusDays(ANALYSIS_DAYS - 1L);
        return all.stream()
                .filter(transaction -> !ServiceTime.toLocalDate(transaction.getTransactedAt()).isBefore(from))
                .toList();
    }

    @Transactional
    public SpendingAnalysisResponse save(AnalysisContext context, SpendingMissionAnalyzeResponse response,
                                         String rawResponse) {
        SpendingMissionAnalyzeResponse.SpendingAnalysisPayload payload = response.spendingAnalysis();

        SpendingAnalysis analysis = spendingAnalysisRepository.save(SpendingAnalysis.builder()
                .userId(context.userId())
                .analysisStartDate(context.request().weekStartDate())
                .analysisEndDate(context.request().weekEndDate())
                .topCategoryCode(CategoryCode.valueOf(payload.topCategoryCode()))
                .recurringExpense(payload.recurringExpense())
                .reducibleAmount(payload.reducibleAmount())
                .summary(payload.summary())
                .aiRawResponse(rawResponse)
                .modelVersion(response.modelVersion())
                .build());

        List<SpendingMissionAnalyzeResponse.CategorySpending> sorted = payload.categorySpending().stream()
                .sorted(Comparator.comparingLong(
                        SpendingMissionAnalyzeResponse.CategorySpending::amount).reversed())
                .toList();
        spendingCategoryAmountRepository.saveAll(IntStream.range(0, sorted.size())
                .mapToObj(index -> SpendingCategoryAmount.builder()
                        .analysisId(analysis.getId())
                        .categoryCode(CategoryCode.valueOf(sorted.get(index).categoryCode()))
                        .amount(sorted.get(index).amount())
                        .ratio(sorted.get(index).ratio())
                        .displayOrder(index + 1)
                        .build())
                .toList());

        spendingKeyInsightRepository.saveAll(IntStream.range(0, payload.keyInsights().size())
                .mapToObj(index -> SpendingKeyInsight.builder()
                        .analysisId(analysis.getId())
                        .displayOrder(index + 1)
                        .content(payload.keyInsights().get(index))
                        .build())
                .toList());

        SpendingMissionAnalyzeResponse.MissionPayload mission = response.mission();
        behaviorMissionRepository.save(BehaviorMission.builder()
                .userId(context.userId())
                .spendingAnalysisId(analysis.getId())
                .title(mission.title())
                .description(mission.description())
                .reason(mission.reason())
                .targetAmount(mission.targetAmount())
                .durationDays(mission.durationDays())
                .dueDate(ServiceTime.today().plusDays(mission.durationDays()))
                .modelVersion(response.modelVersion())
                .build());

        return analysisResponse(analysis);
    }

    @Transactional(readOnly = true)
    public SpendingAnalysisResponse getMyAnalysis(Long userId) {
        return analysisResponse(requireAnalysis(userId));
    }

    @Transactional(readOnly = true)
    public BehaviorMissionResponse getCurrentMission(Long userId) {
        BehaviorMission mission = behaviorMissionRepository.findFirstByUserIdOrderByIdDesc(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MISSION_NOT_FOUND));
        return missionResponse(mission);
    }

    @Transactional
    public BehaviorMissionResponse start(Long userId, Long missionId) {
        BehaviorMission mission = requireOwnedMission(userId, missionId);
        if (mission.isCompleted()) {
            throw new BusinessException(ErrorCode.MISSION_ALREADY_COMPLETED);
        }
        if (mission.getStatus() != MissionStatus.PENDING) {
            throw new BusinessException(ErrorCode.MISSION_ALREADY_STARTED);
        }
        mission.start();
        return missionResponse(mission);
    }

    @Transactional
    public BehaviorMissionResponse complete(Long userId, Long missionId) {
        BehaviorMission mission = requireOwnedMission(userId, missionId);
        if (mission.isCompleted()) {
            throw new BusinessException(ErrorCode.MISSION_ALREADY_COMPLETED);
        }

        mission.complete(pensionAssetCalculator.futureValue(
                mission.getTargetAmount(),
                pensionProperties.expectedReturnRate(),
                pensionProperties.contributionYears()));

        return missionResponse(mission);
    }

    @Transactional(readOnly = true)
    public MissionCompletionResponse getCompletions(Long userId, int year) {
        OffsetDateTime from = LocalDate.of(year, 1, 1)
                .atStartOfDay(ServiceTime.ZONE).toOffsetDateTime();
        OffsetDateTime to = LocalDate.of(year, 12, 31)
                .atTime(23, 59, 59).atZone(ServiceTime.ZONE).toOffsetDateTime();

        return MissionCompletionResponse.of(year,
                behaviorMissionRepository.findAllByUserIdAndStatusAndCompletedAtBetweenOrderByCompletedAtDesc(
                        userId, MissionStatus.COMPLETED, from, to));
    }

    private BehaviorMission requireOwnedMission(Long userId, Long missionId) {
        BehaviorMission mission = behaviorMissionRepository.findById(missionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MISSION_NOT_FOUND));
        if (!mission.isOwnedBy(userId)) {
            throw new BusinessException(ErrorCode.MISSION_NOT_FOUND);
        }
        return mission;
    }

    private SpendingAnalysis requireAnalysis(Long userId) {
        return spendingAnalysisRepository.findFirstByUserIdOrderByIdDesc(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SPENDING_ANALYSIS_NOT_FOUND));
    }

    private SpendingAnalysisResponse analysisResponse(SpendingAnalysis analysis) {
        return SpendingAnalysisResponse.of(analysis, categorySpendingOf(analysis.getId()),
                spendingKeyInsightRepository.findAllByAnalysisIdOrderByDisplayOrderAsc(analysis.getId()));
    }

    private BehaviorMissionResponse missionResponse(BehaviorMission mission) {
        SpendingAnalysis analysis = spendingAnalysisRepository.findById(mission.getSpendingAnalysisId())
                .orElseThrow(() -> new BusinessException(ErrorCode.SPENDING_ANALYSIS_NOT_FOUND));

        BigDecimal topRatio = spendingCategoryAmountRepository
                .findAllByAnalysisIdOrderByDisplayOrderAsc(analysis.getId()).stream()
                .filter(amount -> amount.getCategoryCode() == analysis.getTopCategoryCode())
                .map(SpendingCategoryAmount::getRatio)
                .findFirst()
                .orElse(BigDecimal.ZERO);

        return BehaviorMissionResponse.of(mission, analysis.getTopCategoryCode(), topRatio, ServiceTime.today());
    }

    private List<CategorySpendingResponse> categorySpendingOf(Long analysisId) {
        Map<CategoryCode, SpendingCategoryAmount> saved = spendingCategoryAmountRepository
                .findAllByAnalysisIdOrderByDisplayOrderAsc(analysisId).stream()
                .collect(Collectors.toMap(SpendingCategoryAmount::getCategoryCode, Function.identity()));

        List<CategorySpendingResponse> items = new ArrayList<>();
        for (SpendingCategoryAmount amount : spendingCategoryAmountRepository
                .findAllByAnalysisIdOrderByDisplayOrderAsc(analysisId)) {
            items.add(SpendingAnalysisResponse.item(amount));
        }
        for (CategoryCode category : CategoryCode.values()) {
            if (!saved.containsKey(category)) {
                items.add(new CategorySpendingResponse(
                        CodeName.of(category, category.getDisplayName()), 0L, BigDecimal.ZERO));
            }
        }
        return items;
    }
}
