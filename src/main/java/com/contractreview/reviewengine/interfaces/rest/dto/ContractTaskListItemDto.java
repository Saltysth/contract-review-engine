package com.contractreview.reviewengine.interfaces.rest.dto;

import com.contractreview.reviewengine.domain.enums.ExecutionStage;
import com.contractreview.reviewengine.domain.enums.ReviewType;
import com.contractreview.reviewengine.domain.enums.TaskStatus;
import com.contractreview.reviewengine.domain.valueobject.ReviewProgress;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 合同任务列表项DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "合同任务列表项")
public class ContractTaskListItemDto {

    /**
     * JPQL查询专用构造函数
     */
    public ContractTaskListItemDto(
        String taskId,
        Long contractId,
        String taskName,
        ReviewType contractType,
        TaskStatus status,
        ExecutionStage currentStage,
        String errorMessage,
        LocalDateTime startTime,
        LocalDateTime completedAt,
        LocalDateTime createdTime
    ) {
        this.taskId = taskId;
        this.contractId = contractId;
        this.taskName = taskName;
        this.contractType = contractType;
        this.status = status;
        this.currentStage = currentStage;
        this.errorMessage = errorMessage;
        this.startTime = startTime;
        this.completedAt = completedAt;
        this.createdTime = createdTime;
        // progress和progressDetail在后续通过方法调用设置
    }

    @Schema(description = "任务ID", example = "123456")
    private String taskId;

    @Schema(description = "合同ID", example = "789")
    private Long contractId;

    @Schema(description = "任务名称", example = "测试合同A")
    private String taskName;

    @Schema(description = "合同类型", example = "全量审查")
    private ReviewType contractType;

    @Schema(description = "任务状态", example = "执行中")
    private TaskStatus status;

    @Schema(description = "当前执行阶段", example = "条款抽取")
    private ExecutionStage currentStage;

    @Schema(description = "任务进度", example = "42%")
    private String progress;

    @Schema(description = "进度对象")
    private ReviewProgress progressDetail;

    @Schema(description = "错误信息", example = "文件解析失败")
    private String errorMessage;

    @Schema(description = "开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @Schema(description = "完成时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime completedAt;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdTime;
}