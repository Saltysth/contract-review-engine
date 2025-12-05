package com.contractreview.reviewengine.interfaces.rest.dto;

import com.contract.common.constant.ContractStatus;
import com.contract.common.constant.ContractType;
import com.contractreview.reviewengine.domain.enums.PromptTemplateType;
import com.contractreview.reviewengine.domain.enums.ReviewType;
import com.contractreview.reviewengine.domain.enums.ReviewTypeDetail;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 合同审查请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "合同审查请求")
public class ContractReviewRequestDto {
    ///
    /// contract
    ///

    @Schema(description = "合同标题", example = "合同标题")
    @NotBlank(message = "合同标题不能为空")
    private String contractTitle;


    ///
    /// task
    ///
    @Schema(description = "任务优先级", example = "5", minimum = "1", maximum = "10")
    @Builder.Default
    private Integer priority = 5;

    @Schema(description = "超时时间(分钟)", example = "30")
    @Builder.Default
    private Integer timeoutMinutes = 30;

    @Schema(description = "最大重试次数", example = "3")
    @Builder.Default
    private Integer maxRetries = 3;

    @Schema(description = "重试间隔(秒)", example = "60")
    @Builder.Default
    private Integer retryIntervalSeconds = 60;


    ///
    /// contract_task
    ///
    @Positive(message = "合同ID不能为空")
    @Schema(description = "合同ID", required = true)
    private Long contractId;

    @NotBlank(message = "文件路径uuid")
    @Schema(description = "文件uuid", example = "127893217892", required = true)
    private String fileUuid;

    @Schema(description = "审查类型")
    private ReviewType reviewType;

    @Schema(description = "合同类型")
    private ContractType contractType;

    /**
     * 上传文件后默认创建的就是draft的合同，只有在完全配置之后才是正式任务
     */
    @Schema(description = "合同状态")
    private boolean isDraft;

    @Schema(description = "提示词模版")
    private PromptTemplateType promptTemplate;

    @Schema(description = "是否启用术语库")
    private Boolean enableTerminology;

    @Schema(description = "自定义选择审查项")
    private List<ReviewTypeDetail> customSelectedReviewTypes;

    @Schema(description = "合同审查规则", example = "1,2,3")
    private List<String> reviewRules;

    @Schema(description = "业务标签列表", example = "娱乐、游戏、家庭")
    private List<String> businessTags;

    @Schema(description = "行业", example = "煤矿业")
    private String industry;

    @Schema(description = "币种", example = "人民币")
    private String currency;
    
}