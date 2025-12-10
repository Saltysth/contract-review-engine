package com.contractreview.reviewengine.interfaces.rest.dto;

import com.contractreview.reviewengine.interfaces.rest.dto.report.EvidenceDto;
import com.contractreview.reviewengine.interfaces.rest.dto.report.RuleResultDto;
import com.contractreview.reviewengine.interfaces.rest.dto.report.StatisticsDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 报告详情响应DTO
 *
 * @author SaltyFish
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "报告详情响应")
public class ReportDetailDto {

    @Schema(description = "报告ID")
    private String id;

    @Schema(description = "任务ID")
    private Long taskId;

    @Schema(description = "合同标题")
    private String contractTitle;

    @Schema(description = "整体风险等级: high/medium/low")
    private String riskLevel;

    @Schema(description = "报告总结")
    private String summary;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;

    @Schema(description = "统计信息")
    private StatisticsDto statistics;

    @Schema(description = "规则结果列表")
    private java.util.List<RuleResultDto> ruleResults;

    @Schema(description = "证据库")
    private Map<String, EvidenceDto> evidences;
}