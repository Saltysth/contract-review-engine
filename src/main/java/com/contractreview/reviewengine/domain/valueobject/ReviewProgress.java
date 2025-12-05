package com.contractreview.reviewengine.domain.valueobject;

import com.contractreview.reviewengine.domain.enums.ExecutionStage;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 审查进度值对象
 *
 * @author SaltyFish
 * @since 1.0.0
 */
@Getter
public class ReviewProgress {

    /**
     * 当前阶段
     */
    private ExecutionStage currentStage;

    /**
     * 进度百分比（0-100）
     */
    private Integer progress;

    /**
     * 构造函数
     * @param currentStage 当前执行阶段
     */
    public ReviewProgress(ExecutionStage currentStage) {
        this.currentStage = currentStage;
        this.progress = calculateProgress();
    }

    /**
     * 私有构造函数，仅用于内部特殊场景
     */
    private ReviewProgress(ExecutionStage currentStage, Integer progress) {
        this.currentStage = currentStage;
        this.progress = progress;
    }

    /**
     * 根据当前阶段计算进度百分比
     * 计算方式：当前阶段在所有阶段列表中的位置 / 总阶段数 * 100
     */
    private Integer calculateProgress() {
        ExecutionStage[] allStages = ExecutionStage.values();
        int totalStages = allStages.length;

        // 找到当前阶段的索引位置
        int currentIndex = 0;
        for (int i = 0; i < totalStages; i++) {
            if (allStages[i] == this.currentStage) {
                currentIndex = i;
                break;
            }
        }

        // 计算进度百分比，当前阶段位置+1表示已完成到这个阶段
        // 例如：如果是第2个阶段（索引1），则进度为 (1+1)/7*100 = 28.57% -> 28%
        int progressPercentage = (int) ((double) (currentIndex + 1) / totalStages * 100);

        // 确保进度在0-100范围内
        return Math.max(0, Math.min(100, progressPercentage));
    }

    /**
     * 更新到指定阶段并重新计算进度
     */
    public void updateStage(ExecutionStage newStage) {
        this.currentStage = newStage;
        this.progress = calculateProgress();
    }

    /**
     * 检查是否已完成
     */
    public boolean isCompleted() {
        return currentStage.isFinalStage();
    }

    /**
     * 创建初始进度
     */
    public static ReviewProgress initial() {
        return new ReviewProgress(ExecutionStage.CONTRACT_CLASSIFICATION);
    }
}