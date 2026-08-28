package com.penfit.penfit.domain.rehearsal.service;

import com.penfit.penfit.domain.financialprofile.repository.FinancialProfileRepository;
import com.penfit.penfit.domain.pensionsetup.entity.PensionSetup;
import com.penfit.penfit.domain.pensionsetup.repository.PensionSetupRepository;
import com.penfit.penfit.domain.rehearsal.dto.RehearsalDetailResponse;
import com.penfit.penfit.domain.rehearsal.dto.RehearsalStartResponse;
import com.penfit.penfit.domain.rehearsal.dto.ScenarioResponse;
import com.penfit.penfit.domain.rehearsal.entity.Rehearsal;
import com.penfit.penfit.domain.rehearsal.entity.RehearsalAnswer;
import com.penfit.penfit.domain.rehearsal.entity.RehearsalScenario;
import com.penfit.penfit.domain.rehearsal.entity.RehearsalScenarioOption;
import com.penfit.penfit.domain.rehearsal.event.RehearsalAnalysisRequested;
import com.penfit.penfit.domain.rehearsal.repository.RehearsalAnswerRepository;
import com.penfit.penfit.domain.rehearsal.repository.RehearsalRepository;
import com.penfit.penfit.domain.rehearsal.repository.RehearsalScenarioOptionRepository;
import com.penfit.penfit.domain.rehearsal.repository.RehearsalScenarioRepository;
import com.penfit.penfit.global.enums.AccountType;
import com.penfit.penfit.global.enums.OptionCode;
import com.penfit.penfit.global.enums.RehearsalOptionCatalog;
import com.penfit.penfit.global.enums.ScenarioCode;
import com.penfit.penfit.global.error.BusinessException;
import com.penfit.penfit.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RehearsalService {

    private static final String OPTION_LABELS = "ABCDEF";

    private final RehearsalRepository rehearsalRepository;
    private final RehearsalAnswerRepository rehearsalAnswerRepository;
    private final RehearsalScenarioRepository rehearsalScenarioRepository;
    private final RehearsalScenarioOptionRepository rehearsalScenarioOptionRepository;
    private final FinancialProfileRepository financialProfileRepository;
    private final PensionSetupRepository pensionSetupRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public RehearsalStartResponse start(Long userId) {
        if (!financialProfileRepository.existsByUserId(userId)) {
            throw new BusinessException(ErrorCode.FINANCIAL_PROFILE_NOT_FOUND);
        }
        PensionSetup setup = pensionSetupRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PENSION_SETUP_NOT_FOUND));

        Rehearsal rehearsal = rehearsalRepository.save(Rehearsal.builder()
                .userId(userId)
                .previewFutureAsset(setup.getPreviewFutureAsset())
                .build());

        return RehearsalStartResponse.of(rehearsal, totalScenarios());
    }

    @Transactional(readOnly = true)
    public List<ScenarioResponse> getScenarios(Long userId, Long rehearsalId) {
        requireOwnedRehearsal(userId, rehearsalId);
        boolean irpUser = pensionSetupRepository.findByUserId(userId)
                .map(setup -> setup.getAccountType() == AccountType.INDIVIDUAL_IRP)
                .orElse(false);

        Map<ScenarioCode, List<RehearsalScenarioOption>> optionsByScenario =
                rehearsalScenarioOptionRepository.findAllByOrderByScenarioCodeAscDisplayOrderAsc().stream()
                        .collect(Collectors.groupingBy(RehearsalScenarioOption::getScenarioCode));

        return rehearsalScenarioRepository.findAllByOrderByDisplayOrderAsc().stream()
                .map(scenario -> toResponse(scenario, optionsByScenario.get(scenario.getScenarioCode()), irpUser))
                .toList();
    }

    @Transactional
    public RehearsalDetailResponse saveAnswer(Long userId, Long rehearsalId, ScenarioCode scenarioCode,
                                              OptionCode optionCode) {
        Rehearsal rehearsal = requireOwnedRehearsal(userId, rehearsalId);
        if (!rehearsal.isInProgress()) {
            throw new BusinessException(ErrorCode.REHEARSAL_NOT_IN_PROGRESS);
        }

        RehearsalOptionCatalog.require(scenarioCode, optionCode);

        if (rehearsalAnswerRepository.existsByRehearsalIdAndScenarioCode(rehearsalId, scenarioCode)) {
            throw new BusinessException(ErrorCode.ANSWER_ALREADY_EXISTS);
        }

        rehearsalAnswerRepository.save(RehearsalAnswer.builder()
                .rehearsalId(rehearsalId)
                .scenarioCode(scenarioCode)
                .optionCode(optionCode)
                .build());

        return detailOf(rehearsal);
    }

    @Transactional
    public RehearsalDetailResponse complete(Long userId, Long rehearsalId) {
        Rehearsal rehearsal = requireOwnedRehearsal(userId, rehearsalId);
        if (!rehearsal.isInProgress()) {
            throw new BusinessException(ErrorCode.REHEARSAL_NOT_IN_PROGRESS);
        }
        if (rehearsalAnswerRepository.countByRehearsalId(rehearsalId) != totalScenarios()) {
            throw new BusinessException(ErrorCode.REHEARSAL_ANSWERS_INCOMPLETE);
        }

        rehearsal.startAnalysis();
        eventPublisher.publishEvent(new RehearsalAnalysisRequested(rehearsalId));

        return detailOf(rehearsal);
    }

    @Transactional
    public RehearsalDetailResponse retryAnalysis(Long userId, Long rehearsalId) {
        Rehearsal rehearsal = requireOwnedRehearsal(userId, rehearsalId);
        if (!rehearsal.isFailed()) {
            throw new BusinessException(ErrorCode.REHEARSAL_RETRY_NOT_ALLOWED);
        }

        rehearsal.retryAnalysis();
        eventPublisher.publishEvent(new RehearsalAnalysisRequested(rehearsalId));

        return detailOf(rehearsal);
    }

    @Transactional(readOnly = true)
    public RehearsalDetailResponse getDetail(Long userId, Long rehearsalId) {
        return detailOf(requireOwnedRehearsal(userId, rehearsalId));
    }

    private RehearsalDetailResponse detailOf(Rehearsal rehearsal) {
        List<RehearsalAnswer> answers = rehearsalAnswerRepository.findByRehearsalId(rehearsal.getId());
        return RehearsalDetailResponse.of(rehearsal, answers, totalScenarios());
    }

    private Rehearsal requireOwnedRehearsal(Long userId, Long rehearsalId) {
        Rehearsal rehearsal = rehearsalRepository.findById(rehearsalId)
                .orElseThrow(() -> new BusinessException(ErrorCode.REHEARSAL_NOT_FOUND));
        if (!rehearsal.isOwnedBy(userId)) {
            throw new BusinessException(ErrorCode.REHEARSAL_FORBIDDEN);
        }
        return rehearsal;
    }

    private ScenarioResponse toResponse(RehearsalScenario scenario, List<RehearsalScenarioOption> options,
                                        boolean irpUser) {
        List<ScenarioResponse.OptionResponse> optionResponses = options.stream()
                .map(option -> new ScenarioResponse.OptionResponse(
                        option.getOptionCode().name(),
                        option.getDisplayOrder(),
                        String.valueOf(OPTION_LABELS.charAt(option.getDisplayOrder() - 1)),
                        option.getTitle(),
                        option.getDescription()))
                .toList();

        return new ScenarioResponse(
                scenario.getScenarioCode().name(),
                scenario.getDisplayOrder(),
                scenario.getTitle(),
                scenario.getBadge(),
                scenario.getSituation(),
                scenario.getQuestion(),
                scenario.getBaselineContribution(),
                scenario.getContextCards(),
                irpUser ? scenario.getIrpNotice() : null,
                optionResponses);
    }

    private int totalScenarios() {
        return ScenarioCode.values().length;
    }
}
