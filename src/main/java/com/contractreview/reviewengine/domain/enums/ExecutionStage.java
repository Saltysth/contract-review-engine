package com.contractreview.reviewengine.domain.enums;

import lombok.Getter;

/**
 * 执行阶段枚举
 * 
 * @author SaltyFish
 */
@Getter
public enum ExecutionStage {

    CONTRACT_CLASSIFICATION("合同分类"),
    CLAUSE_EXTRACTION("条款抽取"),
    KNOWLEDGE_MATCHING("知识库匹配"),
    RULE_MATCHING("规则匹配"),
    MODEL_REVIEW("模型审查"),
    RESULT_VALIDATION("结果校验"),
    REPORT_GENERATION("报告生成"),
    REVIEW_COMPLETED("审查完毕");
    
    private final String displayName;

    ExecutionStage(String displayName) {
        this.displayName = displayName;
    }
    
    /**
     * 获取下一个阶段
     */
    public ExecutionStage nextStage() {
        ExecutionStage[] stages = values();
        int currentIndex = this.ordinal();
        return currentIndex < stages.length - 1 ? stages[currentIndex + 1] : this;
    }
    
    /**
     * 是否为最终阶段
     */
    public boolean isFinalStage() {
        return this == REVIEW_COMPLETED;
    }
}