package com.contractreview.reviewengine.domain.valueobject;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 合同元数据值对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContractMetadata {

    private Long contractId;

    private String fileUuid;

    private List<String> businessTags;

    /**
     * 创建合同元数据
     */
    public static ContractMetadata of(Long contractId, String fileUuid) {
        return ContractMetadata.builder()
                .contractId(contractId)
                .fileUuid(fileUuid)
                .build();
    }

    /**
     * 验证元数据有效性
     */
    public boolean isValid() {
        return contractId != null && fileUuid != null && !fileUuid.trim().isEmpty();
    }

}