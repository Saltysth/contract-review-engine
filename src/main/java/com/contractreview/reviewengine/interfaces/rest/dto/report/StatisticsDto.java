package com.contractreview.reviewengine.interfaces.rest.dto.report;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统计信息DTO
 *
 * @author SaltyFish
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "统计信息")
public class StatisticsDto {

    @Schema(description = "总条款数")
    private Integer totalClauses;

    @Schema(description = "已审查条款数")
    private Integer reviewed;

    @Schema(description = "高风险数量 - 对应RiskLevel.HIGH")
    private Integer high;

    @Schema(description = "中风险数量 - 对应RiskLevel.MEDIUM")
    private Integer medium;

    @Schema(description = "低风险数量 - 对应RiskLevel.LOW")
    private Integer low;

    @Schema(description = "无问题数量 - 对应RiskLevel.NO_RISK")
    private Integer noIssue;

    @Schema(description = "关键发现列表（3-5条）")
    private java.util.List<String> keyFindings;

    @Schema(description = "核心建议列表（3-5条）")
    private java.util.List<String> recommendations;
}