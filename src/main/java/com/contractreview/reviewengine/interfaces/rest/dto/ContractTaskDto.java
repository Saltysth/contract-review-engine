package com.contractreview.reviewengine.interfaces.rest.dto;

import com.contractreview.reviewengine.domain.enums.PromptTemplateType;
import com.contractreview.reviewengine.domain.enums.ReviewType;
import com.contract.common.enums.ReviewTypeDetail;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * 合同任务DTO
 */
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "合同审查任务信息")
public class ContractTaskDto extends TaskDto {

    @Schema(description = "合同ID")
    private Long contractId;

    @Schema(description = "文件uuid")
    private String fileUuid;

    @Schema(description = "业务标签", nullable = true)
    private List<String> businessTags;

    @Schema(description = "审查类型")
    private ReviewType reviewType;

    @Schema(description = "个性化选择的审查类型条目")
    private List<ReviewTypeDetail> customSelectedReviewTypes;

    @Schema(description = "所属行业", nullable = true)
    private String industry;

    @Schema(description = "币种", nullable = true)
    private String currency;

    @Schema(description = "合同类型")
    private String contractType;

    @Schema(description = "置信度")
    private Double confidence;

    @Schema(description = "审查规则")
    private ReviewRuleDTO reviewRules;

    @Schema(description = "提示词模板")
    private PromptTemplateType promptTemplateType;

    @Schema(description = "术语库启用")
    private Boolean enableTerminology;
}