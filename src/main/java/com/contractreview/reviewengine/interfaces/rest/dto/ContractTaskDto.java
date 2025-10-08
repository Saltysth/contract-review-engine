package com.contractreview.reviewengine.interfaces.rest.dto;

import com.contractreview.reviewengine.domain.model.ContractTask;
import com.contractreview.reviewengine.domain.enums.ReviewType;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.Map;

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
    
    @Schema(description = "合同ID", example = "CONTRACT-2024-001")
    private Long contractId;
    
    @Schema(description = "文件路径", example = "/contracts/2024/contract-001.pdf")
    private String filePath;
    
    @Schema(description = "文件哈希", example = "sha256:abc123...")
    private String fileHash;
    
    @Schema(description = "审查类型")
    private ReviewType reviewType;
    

}