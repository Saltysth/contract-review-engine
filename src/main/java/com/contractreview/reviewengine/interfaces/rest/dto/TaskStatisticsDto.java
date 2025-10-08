package com.contractreview.reviewengine.interfaces.rest.dto;

import com.contractreview.reviewengine.domain.enums.TaskStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 任务统计DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "任务统计信息")
public class TaskStatisticsDto {
    
    @Schema(description = "各状态任务数量")
    private Map<TaskStatus, Long> statusCounts;
    
    @Schema(description = "总任务数", example = "100")
    private Long totalTasks;
    
    @Schema(description = "待处理任务数", example = "10")
    private Long pendingTasks;
    
    @Schema(description = "运行中任务数", example = "5")
    private Long runningTasks;
    
    @Schema(description = "已完成任务数", example = "80")
    private Long completedTasks;
    
    @Schema(description = "失败任务数", example = "3")
    private Long failedTasks;
    
    @Schema(description = "已取消任务数", example = "2")
    private Long cancelledTasks;
    
    /**
     * 从统计数据转换为DTO
     */
    public static TaskStatisticsDto fromStatistics(List<Object[]> statistics) {
        Map<TaskStatus, Long> statusCounts = new HashMap<>();
        
        for (Object[] stat : statistics) {
            TaskStatus status = (TaskStatus) stat[0];
            Long count = ((Number) stat[1]).longValue();
            statusCounts.put(status, count);
        }
        
        return TaskStatisticsDto.builder()
                .statusCounts(statusCounts)
                .totalTasks(statusCounts.values().stream().mapToLong(Long::longValue).sum())
                .pendingTasks(statusCounts.getOrDefault(TaskStatus.PENDING, 0L))
                .runningTasks(statusCounts.getOrDefault(TaskStatus.RUNNING, 0L))
                .completedTasks(statusCounts.getOrDefault(TaskStatus.COMPLETED, 0L))
                .failedTasks(statusCounts.getOrDefault(TaskStatus.FAILED, 0L))
                .cancelledTasks(statusCounts.getOrDefault(TaskStatus.CANCELLED, 0L))
                .build();
    }
}