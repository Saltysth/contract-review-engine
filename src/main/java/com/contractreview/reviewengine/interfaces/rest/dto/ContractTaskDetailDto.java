package com.contractreview.reviewengine.interfaces.rest.dto;

import com.contract.common.enums.ReviewTypeDetail;
import com.contractreview.reviewengine.domain.enums.ExecutionStage;
import com.contractreview.reviewengine.domain.enums.PromptTemplateType;
import com.contractreview.reviewengine.domain.enums.ReviewType;
import com.contractreview.reviewengine.domain.enums.TaskStatus;
import com.contractreview.reviewengine.domain.enums.TaskType;
import com.contractreview.reviewengine.domain.valueobject.AuditInfo;
import com.contractreview.reviewengine.domain.valueobject.ReviewProgress;
import com.contractreview.reviewengine.domain.valueobject.TaskConfiguration;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.logging.log4j.util.Strings;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 合同任务详情DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "合同审查任务详情（整合全量属性）")
public class ContractTaskDetailDto {

    @Schema(description = "任务ID", example = "55446655440000")
    private String taskId;

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

    @Schema(description = "审查进度（详情对象）")
    private ReviewProgress progressDetail;

    @Schema(description = "审计信息")
    private AuditInfo auditInfo;

    @Schema(description = "合同ID")
    private Long contractId;

    @Schema(description = "文件UUID", example = "f897e548-xxxx-xxxx-xxxx-xxxxxxxxxxxx")
    private String fileUuid;

    @Schema(description = "业务标签", nullable = true)
    private List<String> businessTags;

    @Schema(description = "审查类型")
    private ReviewType reviewType; // 与ListItem的contractType对齐（ListItem命名不精准，统一为reviewType）

    @Schema(description = "个性化选择的审查类型条目")
    private List<ReviewTypeDetail> customSelectedReviewTypes;

    @Schema(description = "所属行业", nullable = true)
    private String industry;

    @Schema(description = "币种", nullable = true)
    private String currency;

    @Schema(description = "合同类型（文本描述）")
    private String contractType;

    @Schema(description = "置信度")
    private Double confidence;

    @Schema(description = "提示词模板")
    private PromptTemplateType promptTemplateType;

    @Schema(description = "术语库启用")
    private Boolean enableTerminology;

    @Schema(description = "当前执行阶段", example = "条款抽取")
    private ExecutionStage currentStage;

    @Schema(description = "任务进度（字符串展示，如42%）", example = "42%")
    private String progressStr;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdTime;

    /**
     * JPQL查询专用构造函数
     */
    public ContractTaskDetailDto(
        String taskId,
        String taskName,
        TaskType taskType,
        TaskStatus status,
        TaskConfiguration configuration,
        String errorMessage,
        LocalDateTime startTime,
        LocalDateTime completedAt,
        Long contractId,
        String fileUuid,
        List<String> businessTags,
        ReviewType reviewType,
        List<ReviewTypeDetail> customSelectedReviewTypes,
        String industry,
        String currency,
        String contractType,
        BigDecimal typeConfidence,
        PromptTemplateType promptTemplate,
        Boolean enableTerminology,
        ExecutionStage currentStage,
        LocalDateTime createdTime
    ) {
        this.taskId = taskId;
        this.taskName = taskName;
        this.taskType = taskType;
        this.status = status;
        this.configuration = configuration;
        this.errorMessage = errorMessage;
        this.startTime = startTime;
        this.completedAt = completedAt;
        this.contractId = contractId;
        this.fileUuid = fileUuid;
        this.businessTags = businessTags;
        this.reviewType = reviewType;
        this.customSelectedReviewTypes = customSelectedReviewTypes;
        this.industry = industry;
        this.currency = currency;
        this.contractType = contractType;
        this.confidence = typeConfidence != null ? typeConfidence.doubleValue() : null;
        this.promptTemplateType = promptTemplate;
        this.enableTerminology = enableTerminology;
        this.currentStage = currentStage;
        this.createdTime = createdTime;
    }
}