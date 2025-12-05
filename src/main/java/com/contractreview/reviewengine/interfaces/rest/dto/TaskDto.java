package com.contractreview.reviewengine.interfaces.rest.dto;

import com.contractreview.reviewengine.domain.enums.ExecutionStage;
import com.contractreview.reviewengine.domain.enums.TaskStatus;
import com.contractreview.reviewengine.domain.enums.TaskType;
import com.contractreview.reviewengine.domain.valueobject.AuditInfo;
import com.contractreview.reviewengine.domain.valueobject.ReviewProgress;
import com.contractreview.reviewengine.domain.valueobject.TaskConfiguration;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * 任务DTO
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "任务信息")
public class TaskDto {

    @Schema(description = "任务ID", example = "55446655440000")
    private String id;

    @Schema(description = "任务名称", example = "合同审查任务")
    private String taskName;

    @Schema(description = "任务类型")
    private TaskType taskType;

    @Schema(description = "任务状态")
    private TaskStatus status;

    @Schema(description = "任务配置")
    private TaskConfiguration configuration;

    @Schema(description = "错误信息")
    private String errorMessage;

    @Schema(description = "开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @Schema(description = "完成时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime completedAt;

    /**
     * 任务进度
     */
    @Schema(description = "审查进度")
    private ReviewProgress progress;

    @Schema(description = "审计信息")
    private AuditInfo auditInfo;
}