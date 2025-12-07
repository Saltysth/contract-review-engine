package com.contractreview.reviewengine.interfaces.rest.dto;

import com.contract.common.constant.ContractStatus;
import com.contract.common.constant.ContractType;
import com.contractreview.reviewengine.domain.enums.PromptTemplateType;
import com.contractreview.reviewengine.domain.enums.ReviewType;
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
@Schema(description = "合同审查创建请求")
public class ContractReviewCreateRequestDto {
    @NotBlank(message = "文件路径uuid")
    @Schema(description = "文件uuid", example = "127893217892", required = true)
    private String fileUuid;
}