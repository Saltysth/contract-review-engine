package com.contractreview.reviewengine.interfaces.rest.dto;

import com.contractreview.reviewengine.domain.enums.ExecutionStage;
import com.contractreview.reviewengine.domain.valueobject.AuditInfo;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 审查结果DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "合同审查结果")
@Deprecated
public class ReviewResultDto {
    
    @Schema(description = "任务ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private String taskId;
    
    @Schema(description = "当前阶段")
    private ExecutionStage currentStage;

    @Schema(description = "提取的条款")
    private List<Map<String, Object>> extractedClauses;
    
    @Schema(description = "分析结果")
    private Map<String, Object> analysisResult;
    
    @Schema(description = "审查摘要")
    private String summary;
    
    @Schema(description = "建议")
    private String recommendations;
    
    @Schema(description = "审计信息")
    private AuditInfo auditInfo;
    
    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdTime;
    
    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedTime;
    

}