package com.contractreview.reviewengine.interfaces.rest.dto;

import com.contractreview.reviewengine.domain.enums.ReviewType;
import com.contractreview.reviewengine.domain.valueobject.RetryPolicy;
import com.contractreview.reviewengine.domain.valueobject.TaskConfiguration;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import java.util.Map;

/**
 * 合同审查请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "合同审查请求")
public class ContractReviewRequestDto {
    
    @NotBlank(message = "合同ID不能为空")
    @Schema(description = "合同ID", example = "CONTRACT-2024-001", required = true)
    private Long contractId;
    
    @NotBlank(message = "文件路径不能为空")
    @Schema(description = "文件路径", example = "/contracts/2024/contract-001.pdf", required = true)
    private String filePath;
    
    @NotNull(message = "审查类型不能为空")
    @Schema(description = "审查类型", required = true)
    private ReviewType reviewType;
    
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
    

}