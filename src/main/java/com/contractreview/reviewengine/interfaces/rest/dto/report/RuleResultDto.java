package com.contractreview.reviewengine.interfaces.rest.dto.report;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 规则结果DTO
 *
 * @author SaltyFish
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "规则结果")
public class RuleResultDto {

    @Schema(description = "规则结果ID")
    private String id;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "类型: overview/payment/delivery/liability/termination/intellectual_property等")
    private String kind;

    @Schema(description = "风险等级: high/medium/low")
    private String riskLevel;

    @Schema(description = "摘要")
    private String summary;

    @Schema(description = "发现内容列表")
    private List<FindingDto> findings;

    @Schema(description = "改进建议列表")
    private List<String> recommendations;
}