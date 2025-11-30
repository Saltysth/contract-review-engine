package com.contractreview.reviewengine.domain.model;

import lombok.Getter;

/**
 * 审查类型枚举
 * 
 * @author SaltyFish
 */
@Getter
public enum ReviewType {

    MODEL_REVIEW("模型审查", "使用AI模型进行合同审查"),
    MANUAL_REVIEW("人工审查", "由人工专家进行合同审查");
    
    private final String displayName;
    private final String description;
    
    ReviewType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    /**
     * 检查是否为AI模型审查
     */
    public boolean isModelReview() {
        return this == MODEL_REVIEW;
    }

    /**
     * 检查是否为人工审查
     */
    public boolean isManualReview() {
        return this == MANUAL_REVIEW;
    }
}