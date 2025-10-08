package com.contractreview.reviewengine.domain.enums;

/**
 * 审查类型枚举
 * 
 * @author SaltyFish
 */
public enum ReviewType {
    
    FULL_REVIEW("全面审查", "对合同进行全面的风险评估和条款分析"),
    RISK_ASSESSMENT("风险评估", "重点关注合同中的风险条款"),
    COMPLIANCE_CHECK("合规检查", "检查合同是否符合相关法规要求"),
    CLAUSE_ANALYSIS("条款分析", "分析特定条款的内容和影响"),
    CUSTOM("自定义审查", "根据用户配置进行定制化审查");
    
    private final String displayName;
    private final String description;
    
    ReviewType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * 是否需要全面分析
     */
    public boolean requiresFullAnalysis() {
        return this == FULL_REVIEW || this == COMPLIANCE_CHECK;
    }
    
    /**
     * 是否关注风险评估
     */
    public boolean focusOnRisk() {
        return this == FULL_REVIEW || this == RISK_ASSESSMENT;
    }
}