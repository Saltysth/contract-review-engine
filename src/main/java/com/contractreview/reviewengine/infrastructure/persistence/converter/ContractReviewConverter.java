package com.contractreview.reviewengine.infrastructure.persistence.converter;

import com.contractreview.reviewengine.domain.enums.PromptTemplateType;
import com.contractreview.reviewengine.domain.enums.ReviewType;
import com.contractreview.reviewengine.domain.enums.ReviewTypeDetail;
import com.contractreview.reviewengine.domain.model.ContractReview;
import com.contractreview.reviewengine.domain.valueobject.AuditInfo;
import com.contractreview.reviewengine.domain.valueobject.ContractMetadata;
import com.contractreview.reviewengine.domain.valueobject.ReviewConfiguration;
import com.contractreview.reviewengine.infrastructure.persistence.entity.ContractTaskEntity;
import com.contractreview.reviewengine.infrastructure.persistence.projection.ContractTaskProjection;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * 合同审查转换器
 */
@Component
public class ContractReviewConverter {

    /**
     * 领域实体转JPA实体
     */
    public ContractTaskEntity toEntity(ContractReview contractReview) {
        if (contractReview == null) {
            return null;
        }

        ContractTaskEntity entity = new ContractTaskEntity();
        entity.setId(contractReview.getId());
        entity.setTaskId(contractReview.getTaskId());
        entity.setResultData(contractReview.getResultData());
        entity.setAuditInfo(contractReview.getAuditInfo());

        if (contractReview.getContractMetadata() != null) {
            ContractMetadata metadata = contractReview.getContractMetadata();
            entity.setContractId(metadata.getContractId());
            entity.setFileUuid(metadata.getFileUuid());
            entity.setContractTitle(metadata.getContractTitle());
            entity.setBusinessTags(metadata.getBusinessTags());
        }

        if (contractReview.getReviewConfiguration() != null) {
            ReviewConfiguration config = contractReview.getReviewConfiguration();
            entity.setReviewType(config.getReviewType());
            entity.setCustomSelectedReviewTypes(config.getCustomSelectedReviewTypes());
            entity.setIndustry(config.getIndustry());
            entity.setCurrency(config.getCurrency());
            entity.setContractType(config.getContractType());
            entity.setTypeConfidence(config.getTypeConfidence());
            entity.setReviewRules(config.getReviewRules());
            entity.setPromptTemplate(config.getPromptTemplate());
            entity.setEnableTerminology(config.getEnableTerminology());
        }

        return entity;
    }

    /**
     * JPA实体转领域实体
     */
    public ContractReview toDomain(ContractTaskEntity entity) {
        if (entity == null) {
            return null;
        }

        ContractMetadata metadata = ContractMetadata.builder()
                .contractId(entity.getContractId())
                .fileUuid(entity.getFileUuid())
                .contractTitle(entity.getContractTitle())
                .businessTags(entity.getBusinessTags())
                .build();

        ReviewConfiguration config = ReviewConfiguration.builder()
                .reviewType(entity.getReviewType())
                .customSelectedReviewTypes(entity.getCustomSelectedReviewTypes())
                .industry(entity.getIndustry())
                .currency(entity.getCurrency())
                .contractType(entity.getContractType())
                .typeConfidence(entity.getTypeConfidence())
                .reviewRules(entity.getReviewRules())
                .promptTemplate(entity.getPromptTemplate())
                .enableTerminology(entity.getEnableTerminology())
                .build();

        return new ContractReview(
                entity.getId(),
                entity.getTaskId(),
                metadata,
                config,
                entity.getResultData(),
                entity.getAuditInfo()
        );
    }

    /**
     * 投影转领域实体
     */
    public ContractReview toDomain(ContractTaskEntity entity, ContractTaskProjection projection) {
        if (projection == null) {
            return toDomain(entity);
        }

        ContractMetadata metadata = ContractMetadata.builder()
                .contractId(projection.getContractId())
                .fileUuid(projection.getFileUuid())
                .contractTitle(projection.getContractTitle())
                .businessTags(projection.getBusinessTags())
                .build();

        ReviewConfiguration config = ReviewConfiguration.builder()
                .reviewType(projection.getReviewType())
                .industry(projection.getIndustry())
                .currency(projection.getCurrency())
                .contractType(projection.getContractType())
                .typeConfidence(projection.getTypeConfidence())
                .reviewRules(projection.getReviewRules())
                .promptTemplate(projection.getPromptTemplate())
                .enableTerminology(projection.getEnableTerminology())
                .build();

        return new ContractReview(
                projection.getId(),
                projection.getTaskId(),
                metadata,
                config,
                projection.getResultData(),
                projection.getAuditInfo()
        );
    }

}