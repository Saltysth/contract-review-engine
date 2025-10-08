package com.contractreview.reviewengine.interfaces.rest.dto;

import com.contractreview.reviewengine.domain.model.ContractTask;
import com.contractreview.reviewengine.domain.enums.ReviewType;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 合同任务DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "合同审查任务信息")
public class ContractTaskDto extends TaskDto {
    
    @Schema(description = "合同ID", example = "CONTRACT-2024-001")
    private String contractId;
    
    @Schema(description = "文件路径", example = "/contracts/2024/contract-001.pdf")
    private String filePath;
    
    @Schema(description = "文件哈希", example = "sha256:abc123...")
    private String fileHash;
    
    @Schema(description = "审查类型")
    private ReviewType reviewType;
    
    @Schema(description = "元数据")
    private Map<String, Object> metadata;
    
    /**
     * 从领域对象转换为DTO
     */
    public static ContractTaskDto fromDomain(ContractTask contractTask) {
        ContractTaskDto dto = new ContractTaskDto();
        dto.setContractId(contractTask.getContractId());
        dto.setFilePath(contractTask.getFilePath());
        dto.setFileHash(contractTask.getFileHash());
        dto.setReviewType(contractTask.getReviewType());
        dto.setMetadata(contractTask.getMetadata());
        
        // 复制基类属性
        TaskDto baseDto = TaskDto.fromDomain(contractTask);
        dto.setId(baseDto.getId());
        dto.setTaskType(baseDto.getTaskType());
        dto.setStatus(baseDto.getStatus());
        dto.setCurrentStage(baseDto.getCurrentStage());
        dto.setConfiguration(baseDto.getConfiguration());
        dto.setRetryCount(baseDto.getRetryCount());
        dto.setErrorMessage(baseDto.getErrorMessage());
        dto.setProgress(baseDto.getProgress());
        dto.setAuditInfo(baseDto.getAuditInfo());
        dto.setCreatedAt(baseDto.getCreatedAt());
        dto.setUpdatedAt(baseDto.getUpdatedAt());
        
        return dto;
    }
}