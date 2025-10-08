package com.contractreview.reviewengine.domain.enums;

import lombok.Getter;

/**
 * 任务类型枚举
 * 
 * @author SaltyFish
 */
@Getter
public enum TaskType {
    
    CONTRACT_REVIEW("合同审查", "合同内容的自动化审查任务"),
    CLAUSE_EXTRACTION("条款提取", "从合同中提取特定条款"),
    RISK_ANALYSIS("风险分析", "合同风险分析任务"),
    COMPLIANCE_CHECK("合规检查", "合同合规性检查任务"),
    BATCH_REVIEW("批量审查", "批量合同审查任务"),
    IMMEDIATE_REVIEW("即时审查", "立即执行的合同审查任务");
    
    private final String displayName;
    private final String description;
    
    TaskType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
}