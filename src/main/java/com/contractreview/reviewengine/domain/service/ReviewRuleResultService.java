package com.contractreview.reviewengine.domain.service;

import com.contractreview.reviewengine.domain.enums.RiskLevel;
import com.contractreview.reviewengine.domain.model.ReviewRuleResultEntity;
import com.contractreview.reviewengine.domain.model.ReviewRuleResultId;
import com.contractreview.reviewengine.domain.model.ReviewResult;
import com.contractreview.reviewengine.domain.model.ReviewResultId;
import com.contractreview.reviewengine.domain.repository.ReviewRuleResultRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 审查规则结果领域服务
 * 负责处理审查规则结果相关的业务逻辑
 *
 * @author SaltyFish
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewRuleResultService {

    private final ReviewRuleResultRepository reviewRuleResultRepository;

    /**
     * 创建新的审查规则结果
     */
    @Transactional
    public ReviewRuleResultEntity createReviewRuleResult(ReviewResultId reviewResultId,
                                                        String riskName,
                                                        String ruleType,
                                                        RiskLevel riskLevel,
                                                        Double riskScore,
                                                        String summary) {
        // 需要注入ReviewResultRepository来获取ReviewResult对象
        // 这里先创建一个临时的ReviewResult对象，实际使用时需要从数据库获取
        ReviewResult reviewResult = new ReviewResult();
        // TODO: 从repository获取实际的ReviewResult对象
        // reviewResult = reviewResultRepository.findById(reviewResultId)
        //     .orElseThrow(() -> new IllegalArgumentException("ReviewResult not found"));

        ReviewRuleResultEntity ruleResult = ReviewRuleResultEntity.builder()
                .id(ReviewRuleResultId.generate().getValue())
                .reviewResult(reviewResult)
                .riskName(riskName)
                .ruleType(ruleType)
                .riskLevel(riskLevel)
                .riskScore(riskScore)
                .summary(summary)
                .build();

        ReviewRuleResultEntity savedResult = reviewRuleResultRepository.save(ruleResult);
        log.info("Created new review rule result: {} for review result: {}",
                savedResult.getId(), reviewResultId);

        return savedResult;
    }

    /**
     * 更新规则结果的风险等级和评分
     */
    @Transactional
    public void updateRiskLevelAndScore(ReviewRuleResultId ruleResultId,
                                        RiskLevel riskLevel,
                                        Double riskScore) {
        ReviewRuleResultEntity ruleResult = getReviewRuleResultById(ruleResultId);
        ruleResult.setRiskLevel(riskLevel);
        ruleResult.setRiskScore(riskScore);
        reviewRuleResultRepository.save(ruleResult);

        log.info("Updated risk level and score for rule result: {} to level: {} and score: {}",
                ruleResultId, riskLevel, riskScore);
    }

    /**
     * 更新规则结果的发现内容
     */
    @Transactional
    public void updateFindings(ReviewRuleResultId ruleResultId, List<String> findings) {
        ReviewRuleResultEntity ruleResult = getReviewRuleResultById(ruleResultId);
        ruleResult.setFindings(findings);
        reviewRuleResultRepository.save(ruleResult);

        log.info("Updated findings for rule result: {} with {} items", ruleResultId, findings.size());
    }

    /**
     * 更新规则结果的建议
     */
    @Transactional
    public void updateRecommendation(ReviewRuleResultId ruleResultId, List<String> recommendation) {
        ReviewRuleResultEntity ruleResult = getReviewRuleResultById(ruleResultId);
        ruleResult.setRecommendation(recommendation);
        reviewRuleResultRepository.save(ruleResult);

        log.info("Updated recommendation for rule result: {} with {} items", ruleResultId, recommendation.size());
    }

    /**
     * 关联风险条款ID
     */
    @Transactional
    public void associateRiskClause(ReviewRuleResultId ruleResultId, String riskClauseId, String originContractText) {
        ReviewRuleResultEntity ruleResult = getReviewRuleResultById(ruleResultId);
        ruleResult.setRiskClauseId(riskClauseId);
        ruleResult.setOriginContractText(originContractText);
        reviewRuleResultRepository.save(ruleResult);

        log.info("Associated risk clause ID: {} with rule result: {}", riskClauseId, ruleResultId);
    }

    /**
     * 根据ID获取审查规则结果
     */
    @Transactional(readOnly = true)
    public ReviewRuleResultEntity getReviewRuleResultById(ReviewRuleResultId ruleResultId) {
        return reviewRuleResultRepository.findById(ruleResultId)
                .orElseThrow(() -> new IllegalArgumentException("Review rule result not found: " + ruleResultId));
    }

    /**
     * 根据审查结果ID获取所有规则结果
     */
    @Transactional(readOnly = true)
    public List<ReviewRuleResultEntity> getRuleResultsByReviewResultId(ReviewResultId reviewResultId) {
        return reviewRuleResultRepository.findByReviewResultId(reviewResultId);
    }

    /**
     * 根据风险名称获取规则结果
     */
    @Transactional(readOnly = true)
    public List<ReviewRuleResultEntity> getRuleResultsByRiskName(String riskName) {
        return reviewRuleResultRepository.findByRiskName(riskName);
    }

    /**
     * 根据规则类型获取规则结果
     */
    @Transactional(readOnly = true)
    public List<ReviewRuleResultEntity> getRuleResultsByRuleType(String ruleType) {
        return reviewRuleResultRepository.findByRuleType(ruleType);
    }

    /**
     * 根据风险等级获取规则结果
     */
    @Transactional(readOnly = true)
    public List<ReviewRuleResultEntity> getRuleResultsByRiskLevel(RiskLevel riskLevel) {
        return reviewRuleResultRepository.findByRiskLevel(riskLevel);
    }

    /**
     * 获取高风险规则结果
     */
    @Transactional(readOnly = true)
    public List<ReviewRuleResultEntity> getHighRiskRuleResults() {
        return reviewRuleResultRepository.findHighRiskResults(
                List.of(RiskLevel.HIGH, RiskLevel.CRITICAL)
        );
    }

    /**
     * 根据风险评分范围获取规则结果
     */
    @Transactional(readOnly = true)
    public List<ReviewRuleResultEntity> getRuleResultsByScoreRange(Double minScore, Double maxScore) {
        return reviewRuleResultRepository.findByRiskScoreBetween(minScore, maxScore);
    }

    /**
     * 根据风险条款ID获取规则结果
     */
    @Transactional(readOnly = true)
    public Optional<ReviewRuleResultEntity> getRuleResultByRiskClauseId(String riskClauseId) {
        return reviewRuleResultRepository.findByRiskClauseId(riskClauseId);
    }

    /**
     * 搜索包含指定文本的规则结果
     */
    @Transactional(readOnly = true)
    public List<ReviewRuleResultEntity> searchRuleResultsByText(String searchText) {
        return reviewRuleResultRepository.findByOriginContractTextContaining(searchText);
    }

    /**
     * 获取各风险等级的统计信息
     */
    @Transactional(readOnly = true)
    public List<Object[]> getRiskLevelStatistics() {
        return reviewRuleResultRepository.countByRiskLevel();
    }

    /**
     * 获取各规则类型的统计信息
     */
    @Transactional(readOnly = true)
    public List<Object[]> getRuleTypeStatistics() {
        return reviewRuleResultRepository.countByRuleType();
    }

    /**
     * 批量保存审查规则结果
     */
    @Transactional
    public List<ReviewRuleResultEntity> batchSaveRuleResults(List<ReviewRuleResultEntity> ruleResults) {
        List<ReviewRuleResultEntity> savedResults = reviewRuleResultRepository.saveAll(ruleResults);
        log.info("Batch saved {} review rule results", savedResults.size());
        return savedResults;
    }

    /**
     * 删除审查规则结果
     */
    @Transactional
    public boolean deleteReviewRuleResult(ReviewRuleResultId ruleResultId) {
        try {
            reviewRuleResultRepository.deleteById(ruleResultId);
            log.info("Deleted review rule result: {}", ruleResultId);
            return true;
        } catch (Exception e) {
            log.error("Failed to delete review rule result: {}", ruleResultId, e);
            return false;
        }
    }

    /**
     * 根据审查结果ID删除所有相关规则结果
     */
    @Transactional
    public void deleteAllRuleResultsByReviewResultId(ReviewResultId reviewResultId) {
        reviewRuleResultRepository.deleteAllByReviewResultId(reviewResultId);
        log.info("Deleted all rule results for review result: {}", reviewResultId);
    }

    /**
     * 检查规则结果是否存在
     */
    @Transactional(readOnly = true)
    public boolean ruleResultExists(ReviewRuleResultId ruleResultId) {
        return reviewRuleResultRepository.existsById(ruleResultId);
    }

    /**
     * 获取规则结果总数
     */
    @Transactional(readOnly = true)
    public long getRuleResultCount() {
        return reviewRuleResultRepository.count();
    }

    /**
     * 根据合同ID获取所有规则结果
     */
    @Transactional(readOnly = true)
    public List<ReviewRuleResultEntity> getRuleResultsByContractId(Long contractId) {
        return reviewRuleResultRepository.findByContractId(contractId);
    }

    /**
     * 根据任务ID获取所有规则结果
     */
    @Transactional(readOnly = true)
    public List<ReviewRuleResultEntity> getRuleResultsByTaskId(Long taskId) {
        return reviewRuleResultRepository.findByTaskId(taskId);
    }

    /**
     * 计算审查结果的总体风险评分
     */
    @Transactional(readOnly = true)
    public Double calculateOverallRiskScore(ReviewResultId reviewResultId) {
        List<ReviewRuleResultEntity> ruleResults = reviewRuleResultRepository.findByReviewResultId(reviewResultId);

        if (ruleResults.isEmpty()) {
            return 0.0;
        }

        // 计算平均风险评分
        return ruleResults.stream()
                .mapToDouble(rule -> rule.getRiskScore() != null ? rule.getRiskScore() : 0.0)
                .average()
                .orElse(0.0);
    }
}