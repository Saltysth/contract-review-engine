package com.contractreview.reviewengine.interfaces.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 任务进度DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "任务进度信息")
public class TaskProgressDto {

    @Schema(description = "任务ID", example = "task-001")
    private String taskId;

    @Schema(description = "当前阶段", example = "REVIEWING_CLAUSES")
    private String currentStage;

    @Schema(description = "进度百分比", example = "66.7")
    private Double progress;

    @Schema(description = "统计信息")
    private TaskStatisticsDto statistics;

    @Schema(description = "预计剩余时间", example = "8分钟")
    private String estimatedTimeRemaining;

    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @Schema(description = "平均每个条款耗时(毫秒)", example = "35000")
    private Long averageItemDuration;

    /**
     * 任务统计信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "任务统计信息")
    public static class TaskStatisticsDto {

        @Schema(description = "总条款数量", example = "24")
        private Integer total;

        @Schema(description = "已完成条款数量", example = "16")
        private Integer completed;

        @Schema(description = "正在执行条款数量", example = "3")
        private Integer running;

        @Schema(description = "失败条款数量", example = "1")
        private Integer failed;

        @Schema(description = "跳过条款数量", example = "4", implementation = Integer.class)
        private Integer skipped;
    }
}