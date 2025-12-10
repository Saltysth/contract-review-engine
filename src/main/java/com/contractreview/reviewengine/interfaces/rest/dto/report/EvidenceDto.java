package com.contractreview.reviewengine.interfaces.rest.dto.report;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 证据DTO
 *
 * @author SaltyFish
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "证据信息")
public class EvidenceDto {

    @Schema(description = "证据ID")
    private String id;

    @Schema(description = "证据标题")
    private String title;

    @Schema(description = "证据类型: rule/analysis/benchmark/knowledge")
    private String type;

    @Schema(description = "证据简述")
    private String description;

    @Schema(description = "证据详细内容")
    private String content;

    @Schema(description = "证据来源: system-rules/ai-analysis/benchmark-database/legal-knowledge-base等")
    private String source;

    @Schema(description = "版本号（适用于规则类型）")
    private String version;

    @Schema(description = "模型版本（适用于AI分析类型）")
    private String modelVersion;

    @Schema(description = "行业（适用于基准数据类型）")
    private String industry;

    @Schema(description = "样本量（适用于基准数据类型）")
    private Integer sampleSize;

    @Schema(description = "参考文献或法条（适用于知识库类型）")
    private List<String> references;

    @Schema(description = "标准名称（适用于知识库类型）")
    private String standard;
}