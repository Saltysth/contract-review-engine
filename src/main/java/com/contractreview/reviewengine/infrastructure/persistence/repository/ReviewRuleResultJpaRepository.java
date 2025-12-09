package com.contractreview.reviewengine.infrastructure.persistence.repository;

import com.contractreview.reviewengine.domain.enums.RiskLevel;
import com.contractreview.reviewengine.domain.model.ReviewRuleResultEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * ReviewRuleResultEntity JPA仓储接口
 *
 * @author SaltyFish
 */
@Repository
public interface ReviewRuleResultJpaRepository extends JpaRepository<ReviewRuleResultEntity, Long> {

    /**
     * 根据审查结果ID查找规则结果
     */
    List<ReviewRuleResultEntity> findByReviewResultId(Long reviewResultId);

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
    @Query("SELECT r FROM ReviewRuleResultEntity r WHERE r.riskScore BETWEEN :minScore AND :maxScore")
    List<ReviewRuleResultEntity> findByRiskScoreBetween(@Param("minScore") Double minScore,
                                                        @Param("maxScore") Double maxScore);

    /**
     * 查找高风险规则结果
     */
    @Query("SELECT r FROM ReviewRuleResultEntity r WHERE r.riskLevel IN :highRiskLevels")
    List<ReviewRuleResultEntity> findHighRiskResults(@Param("highRiskLevels") List<RiskLevel> highRiskLevels);

    /**
     * 根据风险条款ID查找规则结果
     */
    ReviewRuleResultEntity findByRiskClauseId(String riskClauseId);

    /**
     * 查找包含指定原文的规则结果（模糊查询）
     */
    @Query("SELECT r FROM ReviewRuleResultEntity r WHERE r.originContractText LIKE %:searchText%")
    List<ReviewRuleResultEntity> findByOriginContractTextContaining(@Param("searchText") String searchText);

    /**
     * 根据规则类型和风险等级查找规则结果
     */
    List<ReviewRuleResultEntity> findByRuleTypeAndRiskLevel(String ruleType, RiskLevel riskLevel);

    /**
     * 统计各风险等级的规则结果数量
     */
    @Query("SELECT r.riskLevel, COUNT(r) FROM ReviewRuleResultEntity r GROUP BY r.riskLevel")
    List<Object[]> countByRiskLevel();

    /**
     * 统计各规则类型的结果数量
     */
    @Query("SELECT r.ruleType, COUNT(r) FROM ReviewRuleResultEntity r GROUP BY r.ruleType")
    List<Object[]> countByRuleType();

    /**
     * 根据合同ID查找所有规则结果（通过关联查询）
     */
    @Query("SELECT r FROM ReviewRuleResultEntity r WHERE r.reviewResult.id IN " +
           "(SELECT rr.id FROM ReviewResult rr WHERE rr.contractId = :contractId)")
    List<ReviewRuleResultEntity> findByContractId(@Param("contractId") Long contractId);

    /**
     * 根据任务ID查找所有规则结果（通过关联查询）
     */
    @Query("SELECT r FROM ReviewRuleResultEntity r WHERE r.reviewResult.taskId = :taskId")
    List<ReviewRuleResultEntity> findByTaskId(@Param("taskId") Long taskId);

    /**
     * 根据审查结果ID删除所有规则结果
     */
    @Modifying
    @Query("DELETE FROM ReviewRuleResultEntity r WHERE r.reviewResult.id = :reviewResultId")
    void deleteAllByReviewResultId(@Param("reviewResultId") Long reviewResultId);

    /**
     * 查找最近创建的规则结果
     */
    @Query("SELECT r FROM ReviewRuleResultEntity r ORDER BY r.id DESC")
    List<ReviewRuleResultEntity> findRecentResults();

    /**
     * 查找包含关键词的摘要
     */
    @Query("SELECT r FROM ReviewRuleResultEntity r WHERE r.summary LIKE %:keyword%")
    List<ReviewRuleResultEntity> findBySummaryContaining(@Param("keyword") String keyword);

    /**
     * 计算平均风险评分
     */
    @Query("SELECT AVG(r.riskScore) FROM ReviewRuleResultEntity r WHERE r.riskScore IS NOT NULL")
    Double calculateAverageRiskScore();

    /**
     * 查找最高风险评分的规则结果
     */
    @Query("SELECT r FROM ReviewRuleResultEntity r WHERE r.riskScore = " +
           "(SELECT MAX(r2.riskScore) FROM ReviewRuleResultEntity r2 WHERE r2.riskScore IS NOT NULL)")
    List<ReviewRuleResultEntity> findHighestRiskScoreResults();

    /**
     * 根据风险等级统计平均评分
     */
    @Query("SELECT r.riskLevel, AVG(r.riskScore) FROM ReviewRuleResultEntity r " +
           "WHERE r.riskScore IS NOT NULL GROUP BY r.riskLevel")
    List<Object[]> calculateAverageScoreByRiskLevel();
}