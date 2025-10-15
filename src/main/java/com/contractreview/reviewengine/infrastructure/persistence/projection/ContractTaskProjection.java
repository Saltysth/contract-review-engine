package com.contractreview.reviewengine.infrastructure.persistence.projection;

import com.contractreview.reviewengine.domain.enums.PromptTemplateType;
import com.contractreview.reviewengine.domain.enums.ReviewType;
import com.contractreview.reviewengine.domain.valueobject.AuditInfo;

import java.math.BigDecimal;
import java.util.List;

/**
 * 合同任务投影接口，用于DTO直接查询
 */
public interface ContractTaskProjection {

    Long getId();

    Long getTaskId();

    Long getContractId();

    String getFileUuid();

    List<String> getBusinessTags();

    ReviewType getReviewType();

    String getIndustry();

    String getCurrency();

    String getContractType();

    BigDecimal getTypeConfidence();

    String getReviewRules();

    PromptTemplateType getPromptTemplate();

    String getResultData();

    Boolean getEnableTerminology();

    AuditInfo getAuditInfo();

}