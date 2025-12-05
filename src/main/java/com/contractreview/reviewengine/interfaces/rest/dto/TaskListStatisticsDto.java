package com.contractreview.reviewengine.interfaces.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 合同任务列表统计信息DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "合同任务列表统计信息")
public class TaskListStatisticsDto {

    @Schema(description = "总任务数", example = "100")
    private Integer totalTasks;

    @Schema(description = "已完成任务数", example = "75")
    private Integer completedTasks;

    @Schema(description = "进行中任务数", example = "15")
    private Integer runningTasks;

    @Schema(description = "平均执行时长(分钟)", example = "25")
    private Integer averageDuration;
}