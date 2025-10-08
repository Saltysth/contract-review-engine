package com.contractreview.reviewengine.domain.repository;

import com.contractreview.reviewengine.domain.model.ReviewResult;
import com.contractreview.reviewengine.domain.model.TaskId;
import com.contractreview.reviewengine.domain.enums.RiskLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 审查结果仓储接口
 */
@Repository
public interface ReviewResultRepository extends JpaRepository<ReviewResult, TaskId> {
    
    /**
     * 查找高风险结果
     */
    @Query("SELECT rr FROM ReviewResult rr WHERE rr.overallRiskLevel IN (:highRiskLevels)")
    List<ReviewResult> findHighRiskResults(@Param("highRiskLevels") List<RiskLevel> highRiskLevels);
    
    /**
     * 根据任务ID查找结果
     */
    Optional<ReviewResult> findByTaskId(TaskId taskId);
    
    /**
     * 查找指定时间范围内的结果
     */
    @Query("SELECT rr FROM ReviewResult rr WHERE rr.createdAt BETWEEN :startTime AND :endTime")
    List<ReviewResult> findByCreatedAtBetween(@Param("startTime") LocalDateTime startTime, 
                                            @Param("endTime") LocalDateTime endTime);
    
    /**
     * 统计各风险等级的数量
     */
    @Query("SELECT rr.overallRiskLevel, COUNT(rr) FROM ReviewResult rr GROUP BY rr.overallRiskLevel")
    List<Object[]> countByRiskLevel();
    
    /**
     * 查找审查得分在指定范围内的结果
     */
    @Query("SELECT rr FROM ReviewResult rr WHERE rr.overallRiskLevel BETWEEN :minScore AND :maxScore")
    List<ReviewResult> findByScoreRange(@Param("minScore") Double minScore, @Param("maxScore") Double maxScore);
}