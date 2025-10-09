package com.contractreview.reviewengine.domain.enums;

import lombok.Getter;

/**
 * 审查类型枚举
 * 
 * @author SaltyFish
 */
@Getter
public enum ReviewType {
    
    FULL_REVIEW("全量审查", "审查所有选中的条款项目，适用于重要合同"),
    RISK_ASSESSMENT("部分审查", "仅审查核心风险项目，适合常规合同"),
    CUSTOM("个性化审查", "自定义选择具体的审查项目");
    
    private final String displayName;
    private final String description;
    
    ReviewType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    

    /**
     * 是否需要全面分析
     */
    public boolean requiresFullAnalysis() {
        return this == FULL_REVIEW;
    }
    
    /**
     * 是否关注风险评估
     */
    public boolean focusOnRisk() {
        return this == FULL_REVIEW;
    }
}