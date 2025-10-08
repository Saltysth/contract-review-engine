package com.contractreview.reviewengine.interfaces.rest.dto;

import com.contractreview.reviewengine.domain.enums.ExecutionStage;
import com.contractreview.reviewengine.domain.enums.TaskType;
import com.contractreview.reviewengine.domain.model.Task;
import com.contractreview.reviewengine.domain.enums.TaskStatus;
import com.contractreview.reviewengine.domain.valueobject.AuditInfo;
import com.contractreview.reviewengine.domain.valueobject.ReviewProgress;
import com.contractreview.reviewengine.domain.valueobject.TaskConfiguration;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 任务DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "任务信息")
public class TaskDto {
    
    @Schema(description = "任务ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private String id;
    
    @Schema(description = "任务类型")
    private TaskType taskType;
    
    @Schema(description = "任务状态")
    private TaskStatus status;
    
    @Schema(description = "当前执行阶段")
    private ExecutionStage currentStage;
    
    @Schema(description = "任务配置")
    private TaskConfiguration configuration;
    
    @Schema(description = "重试次数", example = "0")
    private Integer retryCount;
    
    @Schema(description = "错误信息")
    private String errorMessage;
    
    @Schema(description = "审查进度")
    private ReviewProgress progress;
    
    @Schema(description = "审计信息")
    private AuditInfo auditInfo;
    
    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
    
    /**
     * 从领域对象转换为DTO
     */
    public static TaskDto fromDomain(Task task) {
        return TaskDto.builder()
                .id(task.getId().toString())
                .taskType(task.getTaskType())
                .status(task.getStatus())
                .currentStage(task.getCurrentStage())
                .configuration(task.getConfiguration())
                .retryCount(task.getRetryCount())
                .errorMessage(task.getErrorMessage())
                .progress(task.getProgress())
                .auditInfo(task.getAuditInfo())
                .createdAt(task.getAuditInfo() != null ? task.getAuditInfo().getCreatedAt() : null)
                .updatedAt(task.getAuditInfo() != null ? task.getAuditInfo().getUpdatedAt() : null)
                .build();
    }
}