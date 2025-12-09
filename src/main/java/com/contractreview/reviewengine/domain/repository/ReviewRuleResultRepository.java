package com.contractreview.reviewengine.domain.repository;

import com.contractreview.reviewengine.domain.enums.RiskLevel;
import com.contractreview.reviewengine.domain.model.ReviewResult;
import com.contractreview.reviewengine.domain.model.ReviewRuleResultEntity;
import com.contractreview.reviewengine.domain.model.ReviewRuleResultId;
import com.contractreview.reviewengine.domain.model.ReviewResultId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * 审查规则结果仓储接口
 *
 * @author SaltyFish
 */
public interface ReviewRuleResultRepository {

    /**
     * 保存审查规则结果
     */
    ReviewRuleResultEntity save(ReviewRuleResultEntity reviewRuleResult);

    /**
     * 根据ID查找审查规则结果
     */
    Optional<ReviewRuleResultEntity> findById(ReviewRuleResultId id);

    /**
     * 查找所有审查规则结果
     */
    List<ReviewRuleResultEntity> findAll();

    /**
     * 根据ID列表查找审查规则结果
     */
    List<ReviewRuleResultEntity> findAllById(Iterable<ReviewRuleResultId> ids);

    /**
     * 检查审查规则结果是否存在
     */
    boolean existsById(ReviewRuleResultId id);

    /**
     * 统计总数
     */
    long count();

    /**
     * 根据ID删除审查规则结果
     */
    void deleteById(ReviewRuleResultId id);

    /**
     * 删除审查规则结果
     */
    void delete(ReviewRuleResultEntity reviewRuleResult);

    /**
     * 删除所有审查规则结果
     */
    void deleteAll();

    /**
     * 根据ID列表删除审查规则结果
     */
    void deleteAllById(Iterable<ReviewRuleResultId> ids);

    /**
     * 根据审查结果ID查找规则结果
     */
    List<ReviewRuleResultEntity> findByReviewResultId(ReviewResultId reviewResultId);

    /**
     * 根据风险名称查找规则结果
     */
    List<ReviewRuleResultEntity> findByRiskName(String riskName);

    /**
     * 根据规则类型查找规则结果
     */
    List<ReviewRuleResultEntity> findByRuleType(String ruleType);

    /**
     * 根据风险等级查找规则结果
     */
    List<ReviewRuleResultEntity> findByRiskLevel(RiskLevel riskLevel);

    /**
     * 根据风险评分范围查找规则结果
     */
    List<ReviewRuleResultEntity> findByRiskScoreBetween(Double minScore, Double maxScore);

    /**
     * 查找高风险规则结果
     */
    List<ReviewRuleResultEntity> findHighRiskResults(List<RiskLevel> highRiskLevels);

    /**
     * 根据风险条款ID查找规则结果
     */
    Optional<ReviewRuleResultEntity> findByRiskClauseId(String riskClauseId);

    /**
     * 查找包含指定原文的规则结果（模糊查询）
     */
    List<ReviewRuleResultEntity> findByOriginContractTextContaining(String searchText);

    /**
     * 根据规则类型和风险等级查找规则结果
     */
    List<ReviewRuleResultEntity> findByRuleTypeAndRiskLevel(String ruleType, RiskLevel riskLevel);

    /**
     * 统计各风险等级的规则结果数量
     */
    List<Object[]> countByRiskLevel();

    /**
     * 统计各规则类型的结果数量
     */
    List<Object[]> countByRuleType();

    /**
     * 分页查询审查规则结果
     */
    Page<ReviewRuleResultEntity> findAll(Pageable pageable);

    /**
     * 根据审查结果ID分页查询规则结果
     */
    Page<ReviewRuleResultEntity> findByReviewResultId(ReviewResultId reviewResultId, Pageable pageable);

    /**
     * 根据合同ID查找所有规则结果（通过关联查询）
     */
    List<ReviewRuleResultEntity> findByContractId(Long contractId);

    /**
     * 根据任务ID查找所有规则结果（通过关联查询）
     */
    List<ReviewRuleResultEntity> findByTaskId(Long taskId);

    /**
     * 批量保存审查规则结果
     */
    List<ReviewRuleResultEntity> saveAll(List<ReviewRuleResultEntity> reviewRuleResults);

    /**
     * 根据审查结果ID删除所有规则结果
     */
    void deleteAllByReviewResultId(ReviewResultId reviewResultId);
}