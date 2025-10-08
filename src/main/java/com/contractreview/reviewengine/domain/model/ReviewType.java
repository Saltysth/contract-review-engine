package com.contractreview.reviewengine.domain.model;

import lombok.Getter;

/**
 * 审查类型枚举
 * 
 * @author SaltyFish
 */
@Getter
public enum ReviewType {
    
    FULL_REVIEW("全面审查", "对合同进行全面的风险和合规审查"),
    RISK_ANALYSIS("风险分析", "专注于合同风险识别和评估"),
    COMPLIANCE_CHECK("合规检查", "检查合同是否符合法律法规要求"),
    CLAUSE_EXTRACTION("条款提取", "提取和分析特定类型的合同条款"),
    QUICK_SCAN("快速扫描", "快速识别合同中的关键风险点");
    
    private final String displayName;
    private final String description;
    
    ReviewType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    /**
     * 检查是否需要详细分析
     */
    public boolean requiresDetailedAnalysis() {
        return this == FULL_REVIEW || this == RISK_ANALYSIS;
    }
    
    /**
     * 检查是否需要合规检查
     */
    public boolean requiresComplianceCheck() {
        return this == FULL_REVIEW || this == COMPLIANCE_CHECK;
    }
}