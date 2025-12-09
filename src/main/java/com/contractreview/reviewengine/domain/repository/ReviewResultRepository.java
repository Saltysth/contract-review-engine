package com.contractreview.reviewengine.domain.repository;

import com.contractreview.reviewengine.domain.enums.RiskLevel;
import com.contractreview.reviewengine.domain.model.ReviewResult;
import com.contractreview.reviewengine.domain.model.ReviewResultId;
import com.contractreview.reviewengine.domain.model.TaskId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 审查结果仓储接口
 *
 * @author SaltyFish
 */
public interface ReviewResultRepository {

    /**
     * 保存审查结果
     */
    ReviewResult save(ReviewResult reviewResult);

    /**
     * 根据ID查找审查结果
     */
    Optional<ReviewResult> findById(ReviewResultId id);

    /**
     * 查找所有审查结果
     */
    List<ReviewResult> findAll();

    /**
     * 根据ID列表查找审查结果
     */
    List<ReviewResult> findAllById(Iterable<ReviewResultId> ids);

    /**
     * 检查审查结果是否存在
     */
    boolean existsById(ReviewResultId id);

    /**
     * 统计总数
     */
    long count();

    /**
     * 根据ID删除审查结果
     */
    void deleteById(ReviewResultId id);

    /**
     * 删除审查结果
     */
    void delete(ReviewResult reviewResult);

    /**
     * 删除所有审查结果
     */
    void deleteAll();

    /**
     * 根据ID列表删除审查结果
     */
    void deleteAllById(Iterable<ReviewResultId> ids);

    /**
     * 根据任务ID查找结果
     */
    Optional<ReviewResult> findByTaskId(TaskId taskId);

    /**
     * 根据合同ID查找结果
     */
    List<ReviewResult> findByContractId(Long contractId);

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
    List<ReviewResult> findHighRiskResults(List<RiskLevel> highRiskLevels);

    /**
     * 查找指定时间范围内的结果
     */
    List<ReviewResult> findByCreatedTimeBetween(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 统计各风险等级的数量
     */
    List<Object[]> countByRiskLevel();

    /**
     * 查找审查得分在指定范围内的结果
     */
    List<ReviewResult> findByRiskScoreRange(Double minScore, Double maxScore);

    /**
     * 分页查询审查结果
     */
    Page<ReviewResult> findAll(Pageable pageable);

    /**
     * 根据合同ID分页查询审查结果
     */
    Page<ReviewResult> findByContractId(Long contractId, Pageable pageable);
}