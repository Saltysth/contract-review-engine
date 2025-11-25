package com.contractreview.reviewengine.domain.valueobject;

import com.contractreview.reviewengine.domain.enums.ExecutionStage;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 审查进度值对象
 *
 * @author SaltyFish
 * @deprecated 此值对象已被弃用。管道阶段处理方式将被简化的直接处理方式替代。
 * 此值对象跟踪多阶段管道的执行进度，在简化的处理方式中不再需要阶段性进度跟踪。
 * 迁移指南：使用简单的任务状态（PENDING、RUNNING、COMPLETED、FAILED）来跟踪审查进度。
 * @since 1.0.0
 * @see com.contractreview.reviewengine.domain.enums.TaskStatus
 */
@Value
@Builder
@Deprecated(since = "1.0.0", forRemoval = true)
public class ReviewProgress {
    
    ExecutionStage currentStage;
    int progressPercentage;
    @Builder.Default
    Map<ExecutionStage, StageResult> stageResults = new HashMap<>();
    @Builder.Default
    LocalDateTime lastUpdated = LocalDateTime.now();
    
    public ReviewProgress(ExecutionStage currentStage, int progressPercentage) {
        this.currentStage = currentStage;
        this.progressPercentage = Math.max(0, Math.min(100, progressPercentage));
        this.stageResults = new HashMap<>();
        this.lastUpdated = LocalDateTime.now();
    }
    
    public ReviewProgress(ExecutionStage currentStage, int progressPercentage, 
                          Map<ExecutionStage, StageResult> stageResults, LocalDateTime lastUpdated) {
        this.currentStage = currentStage;
        this.progressPercentage = Math.max(0, Math.min(100, progressPercentage));
        this.stageResults = new HashMap<>(stageResults != null ? stageResults : new HashMap<>());
        this.lastUpdated = lastUpdated != null ? lastUpdated : LocalDateTime.now();
    }
    
    /**
     * 更新阶段
     */
    public ReviewProgress updateStage(ExecutionStage newStage, int percentage) {
        return new ReviewProgress(newStage, percentage, this.stageResults, LocalDateTime.now());
    }
    
    /**
     * 完成阶段
     */
    public ReviewProgress completeStage(ExecutionStage stage, StageResult result) {
        Map<ExecutionStage, StageResult> newStageResults = new HashMap<>(this.stageResults);
        newStageResults.put(stage, result);
        return new ReviewProgress(this.currentStage, this.progressPercentage, newStageResults, LocalDateTime.now());
    }
    
    /**
     * 检查是否已完成
     */
    public boolean isCompleted() {
        return progressPercentage >= 100 && currentStage.isFinalStage();
    }
    
    /**
     * 获取阶段结果
     */
    public StageResult getStageResult(ExecutionStage stage) {
        return stageResults.get(stage);
    }
    
    /**
     * 检查阶段是否已完成
     */
    public boolean isStageCompleted(ExecutionStage stage) {
        StageResult result = stageResults.get(stage);
        return result != null && result.isSuccess();
    }
    
    /**
     * 创建初始进度
     */
    public static ReviewProgress initial() {
        return new ReviewProgress(ExecutionStage.CONTRACT_CLASSIFICATION, 0);
    }
}