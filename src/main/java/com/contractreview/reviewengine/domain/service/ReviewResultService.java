package com.contractreview.reviewengine.domain.service;

import com.contractreview.reviewengine.domain.enums.RiskLevel;
import com.contractreview.reviewengine.domain.model.ReviewResult;
import com.contractreview.reviewengine.domain.model.ReviewResultId;
import com.contractreview.reviewengine.domain.model.TaskId;
import com.contractreview.reviewengine.domain.repository.ReviewResultRepository;
import com.contractreview.reviewengine.domain.valueobject.Evidence;
import com.contractreview.reviewengine.domain.valueobject.KeyPoint;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 审查结果领域服务
 * 负责处理审查结果相关的业务逻辑
 *
 * @author SaltyFish
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewResultService {

    private final ReviewResultRepository reviewResultRepository;

    /**
     * 创建新的审查结果
     */
    @Transactional
    public ReviewResult createReviewResult(TaskId taskId, Long contractId, String reviewType) {
        ReviewResult reviewResult = ReviewResult.builder()
                .id(ReviewResultId.generate().getValue())
                .taskId(taskId.getValue())
                .contractId(contractId)
                .reviewType(reviewType)
                .createdTime(LocalDateTime.now())
                .build();

        ReviewResult savedResult = reviewResultRepository.save(reviewResult);
        log.info("Created new review result: {} for task: {} and contract: {}",
                savedResult.getId(), taskId, contractId);

        return savedResult;
    }

    /**
     * 更新审查结果的风险等级
     */
    @Transactional
    public void updateRiskLevel(ReviewResultId reviewResultId, String riskLevel) {
        ReviewResult reviewResult = getReviewResultById(reviewResultId);
        reviewResult.setOverallRiskLevel(riskLevel);
        reviewResultRepository.save(reviewResult);

        log.info("Updated risk level for review result: {} to: {}", reviewResultId, riskLevel);
    }

    /**
     * 更新审查结果摘要
     */
    @Transactional
    public void updateSummary(ReviewResultId reviewResultId, String summary) {
        ReviewResult reviewResult = getReviewResultById(reviewResultId);
        reviewResult.setSummary(summary);
        reviewResultRepository.save(reviewResult);

        log.info("Updated summary for review result: {}", reviewResultId);
    }

    /**
     * 更新阶段性结果
     */
    @Transactional
    public void updateStageResult(ReviewResultId reviewResultId, String stageResult) {
        ReviewResult reviewResult = getReviewResultById(reviewResultId);
        reviewResult.setStageResult(stageResult);
        reviewResultRepository.save(reviewResult);

        log.info("Updated stage result for review result: {}", reviewResultId);
    }

    /**
     * 更新模型版本
     */
    @Transactional
    public void updateModelVersion(ReviewResultId reviewResultId, String modelVersion) {
        ReviewResult reviewResult = getReviewResultById(reviewResultId);
        reviewResult.setModelVersion(modelVersion);
        reviewResultRepository.save(reviewResult);

        log.info("Updated model version for review result: {} to: {}", reviewResultId, modelVersion);
    }

    /**
     * 添加关键点
     */
    @Transactional
    public void addKeyPoints(ReviewResultId reviewResultId, List<KeyPoint> keyPoints) {
        ReviewResult reviewResult = getReviewResultById(reviewResultId);
        if (reviewResult.getKeyPoints() == null) {
            reviewResult.setKeyPoints(keyPoints);
        } else {
            reviewResult.getKeyPoints().addAll(keyPoints);
        }
        reviewResultRepository.save(reviewResult);

        log.info("Added {} key points to review result: {}", keyPoints.size(), reviewResultId);
    }

    /**
     * 添加证据
     */
    @Transactional
    public void addEvidences(ReviewResultId reviewResultId, List<Evidence> evidences) {
        ReviewResult reviewResult = getReviewResultById(reviewResultId);
        if (reviewResult.getEvidences() == null) {
            reviewResult.setEvidences(evidences);
        } else {
            reviewResult.getEvidences().addAll(evidences);
        }
        reviewResultRepository.save(reviewResult);

        log.info("Added {} evidences to review result: {}", evidences.size(), reviewResultId);
    }

    /**
     * 根据ID获取审查结果
     */
    @Transactional(readOnly = true)
    public ReviewResult getReviewResultById(ReviewResultId reviewResultId) {
        return reviewResultRepository.findById(reviewResultId)
                .orElseThrow(() -> new IllegalArgumentException("Review result not found: " + reviewResultId));
    }

    /**
     * 根据任务ID获取审查结果
     */
    @Transactional(readOnly = true)
    public Optional<ReviewResult> getReviewResultByTaskId(TaskId taskId) {
        return reviewResultRepository.findByTaskId(taskId);
    }

    /**
     * 根据合同ID获取所有审查结果
     */
    @Transactional(readOnly = true)
    public List<ReviewResult> getReviewResultsByContractId(Long contractId) {
        return reviewResultRepository.findByContractId(contractId);
    }

    /**
     * 获取高风险审查结果
     */
    @Transactional(readOnly = true)
    public List<ReviewResult> getHighRiskReviewResults() {
        return reviewResultRepository.findHighRiskResults(
                List.of(RiskLevel.HIGH, RiskLevel.CRITICAL)
        );
    }

    /**
     * 获取指定时间范围内的审查结果
     */
    @Transactional(readOnly = true)
    public List<ReviewResult> getReviewResultsByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        return reviewResultRepository.findByCreatedTimeBetween(startTime, endTime);
    }

    /**
     * 获取各风险等级的统计信息
     */
    @Transactional(readOnly = true)
    public List<Object[]> getRiskLevelStatistics() {
        return reviewResultRepository.countByRiskLevel();
    }

    /**
     * 检查风险等级是否需要更新
     */
    @Transactional
    public boolean checkAndUpdateRiskLevel(ReviewResultId reviewResultId) {
        ReviewResult reviewResult = getReviewResultById(reviewResultId);

        // 根据规则结果中的最高风险等级更新整体风险等级
        if (reviewResult.getRuleResults() != null && !reviewResult.getRuleResults().isEmpty()) {
            RiskLevel highestRisk = reviewResult.getRuleResults().stream()
                    .map(ruleResult -> ruleResult.getRiskLevel())
                    .max(RiskLevel::compareTo)
                    .orElse(RiskLevel.LOW);

            String newRiskLevel = highestRisk.name();
            if (!newRiskLevel.equals(reviewResult.getOverallRiskLevel())) {
                reviewResult.setOverallRiskLevel(newRiskLevel);
                reviewResultRepository.save(reviewResult);
                log.info("Auto-updated risk level for review result: {} to: {}",
                        reviewResultId, newRiskLevel);
                return true;
            }
        }

        return false;
    }

    /**
     * 删除审查结果
     */
    @Transactional
    public boolean deleteReviewResult(ReviewResultId reviewResultId) {
        try {
            reviewResultRepository.deleteById(reviewResultId);
            log.info("Deleted review result: {}", reviewResultId);
            return true;
        } catch (Exception e) {
            log.error("Failed to delete review result: {}", reviewResultId, e);
            return false;
        }
    }

    /**
     * 检查审查结果是否存在
     */
    @Transactional(readOnly = true)
    public boolean reviewResultExists(ReviewResultId reviewResultId) {
        return reviewResultRepository.existsById(reviewResultId);
    }

    /**
     * 获取审查结果总数
     */
    @Transactional(readOnly = true)
    public long getReviewResultCount() {
        return reviewResultRepository.count();
    }
}