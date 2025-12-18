package com.contractreview.reviewengine.interfaces.rest.dto;

import com.contractreview.reviewengine.domain.enums.RiskLevel;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

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

    @Schema(description = "风险分布", example = "{\"NO_RISK\": 30, \"LOW\": 25, \"MEDIUM\": 20, \"HIGH\": 15, \"CRITICAL\": 10}")
    private Map<RiskLevel, Integer> riskDistribution;

    @Schema(description = "风险趋势(百分比)", example = "15")
    private Integer riskTrend;
}