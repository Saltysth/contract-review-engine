package com.contractreview.reviewengine.domain.model;

import com.contractreview.reviewengine.domain.enums.PromptTemplateType;
import com.contractreview.reviewengine.domain.enums.ReviewType;
import com.contractreview.reviewengine.domain.valueobject.AuditInfo;
import com.contractreview.reviewengine.domain.valueobject.ContractMetadata;
import com.contractreview.reviewengine.domain.valueobject.ReviewConfiguration;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 合同审查领域实体
 *
 * @author SaltyFish
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContractReview {

    private Long id;
    private Long taskId;
    private ContractMetadata contractMetadata;
    private ReviewConfiguration reviewConfiguration;
    private String resultData;
    private AuditInfo auditInfo;

    /**
     * 构造函数
     */
    public ContractReview(Long taskId, ContractMetadata contractMetadata, ReviewConfiguration reviewConfiguration) {
        this.taskId = taskId;
        this.contractMetadata = contractMetadata;
        this.reviewConfiguration = reviewConfiguration;
        this.auditInfo = AuditInfo.create(1L);
    }

    /**
     * 创建合同审查
     */
    public static ContractReview create(Long taskId, Long contractId, String fileUuid) {
        ContractMetadata metadata = ContractMetadata.of(contractId, fileUuid);
        ReviewConfiguration configuration = ReviewConfiguration.defaultConfiguration();

        return new ContractReview(taskId, metadata, configuration);
    }

    /**
     * 更新合同元数据
     */
    public void updateContractMetadata(ContractMetadata metadata) {
        if (metadata != null && metadata.isValid()) {
            this.contractMetadata = metadata;
        }
    }

    /**
     * 更新审查配置
     */
    public void updateReviewConfiguration(ReviewConfiguration configuration) {
        if (configuration != null && configuration.isValid()) {
            this.reviewConfiguration = configuration;
        }
    }

    /**
     * 更新审查规则
     */
    public void updateReviewRules(List<String> rules) {
        if (this.reviewConfiguration != null) {
            this.reviewConfiguration.setReviewRules(rules);
        }
    }

    /**
     * 更新提示模板
     */
    public void updatePromptTemplate(PromptTemplateType template) {
        if (this.reviewConfiguration != null) {
            this.reviewConfiguration.setPromptTemplate(template);
        }
    }

    /**
     * 更新结果数据
     */
    public void updateResultData(String data) {
        this.resultData = data;
    }

    /**
     * 获取合同ID
     */
    public Long getContractId() {
        return contractMetadata != null ? contractMetadata.getContractId() : null;
    }

    /**
     * 获取文件UUID
     */
    public String getFileUuid() {
        return contractMetadata != null ? contractMetadata.getFileUuid() : null;
    }

    
    /**
     * 获取审查类型
     */
    public ReviewType getReviewType() {
        return reviewConfiguration != null ? reviewConfiguration.getReviewType() : null;
    }

    /**
     * 检查是否启用术语库
     */
    public Boolean isEnableTerminology() {
        return reviewConfiguration != null ? reviewConfiguration.getEnableTerminology() : false;
    }

    /**
     * 获取提示模板
     */
    public PromptTemplateType getPromptTemplate() {
        return reviewConfiguration != null ? reviewConfiguration.getPromptTemplate() : null;
    }

    /**
     * 检查审查是否有效
     */
    public boolean isValid() {
        return taskId != null
                && contractMetadata != null && contractMetadata.isValid()
                && reviewConfiguration != null && reviewConfiguration.isValid();
    }

}