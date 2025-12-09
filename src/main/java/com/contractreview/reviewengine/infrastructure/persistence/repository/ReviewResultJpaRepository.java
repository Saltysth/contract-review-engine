package com.contractreview.reviewengine.infrastructure.persistence.repository;

import com.contractreview.reviewengine.domain.enums.RiskLevel;
import com.contractreview.reviewengine.domain.model.ReviewResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ReviewResult JPA仓储接口
 *
 * @author SaltyFish
 */
@Repository
public interface ReviewResultJpaRepository extends JpaRepository<ReviewResult, Long> {

    /**
     * 根据任务ID查找结果
     */
    ReviewResult findByTaskId(Long taskId);

    /**
     * 根据合同ID查找结果
     */
    List<ReviewResult> findByContractId(Long contractId);

    /**
     * 根据合同ID分页查找结果
     */
    org.springframework.data.domain.Page<ReviewResult> findByContractId(Long contractId, org.springframework.data.domain.Pageable pageable);

    /**
     * 根据审查类型查找结果
     */
    List<ReviewResult> findByReviewType(String reviewType);

    /**
     * 根据风险等级查找结果
     */
    List<ReviewResult> findByOverallRiskLevel(String overallRiskLevel);

    /**
     * 查找高风险结果
     */
    @Query("SELECT rr FROM ReviewResult rr WHERE rr.overallRiskLevel IN :highRiskLevels")
    List<ReviewResult> findHighRiskResults(@Param("highRiskLevels") List<String> highRiskLevels);

    /**
     * 查找指定时间范围内的结果
     */
    @Query("SELECT rr FROM ReviewResult rr WHERE rr.createdTime BETWEEN :startTime AND :endTime")
    List<ReviewResult> findByCreatedTimeBetween(@Param("startTime") LocalDateTime startTime,
                                               @Param("endTime") LocalDateTime endTime);

    /**
     * 统计各风险等级的数量
     */
    @Query("SELECT rr.overallRiskLevel, COUNT(rr) FROM ReviewResult rr GROUP BY rr.overallRiskLevel")
    List<Object[]> countByRiskLevel();

    /**
     * 根据模型版本查找结果
     */
    List<ReviewResult> findByModelVersion(String modelVersion);

    /**
     * 查找最近创建的结果
     */
    @Query("SELECT rr FROM ReviewResult rr ORDER BY rr.createdTime DESC")
    List<ReviewResult> findRecentResults();

    /**
     * 查找包含关键词的摘要
     */
    @Query("SELECT rr FROM ReviewResult rr WHERE rr.summary LIKE %:keyword%")
    List<ReviewResult> findBySummaryContaining(@Param("keyword") String keyword);

    /**
     * 查找包含关键词的阶段结果
     */
    @Query("SELECT rr FROM ReviewResult rr WHERE rr.stageResult LIKE %:keyword%")
    List<ReviewResult> findByStageResultContaining(@Param("keyword") String keyword);
}