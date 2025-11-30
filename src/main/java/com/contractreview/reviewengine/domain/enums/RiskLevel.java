package com.contractreview.reviewengine.domain.enums;

import lombok.Getter;

/**
 * 风险等级枚举
 * 
 * @author SaltyFish
 */
@Getter
public enum RiskLevel {
    
    NO_RISK("无风险", "无风险，放心使用", "#28a745"),
    LOW("低风险", "风险较小，建议关注", "#17a2b8"),
    MEDIUM("中等风险", "存在一定风险，需要注意", "#ffc107"),
    HIGH("高风险", "风险较高，需要重点关注", "#fd7e14"),
    CRITICAL("严重风险", "风险严重，需要立即处理", "#dc3545");
    
    private final String displayName;
    private final String description;
    private final String colorCode;
    
    RiskLevel(String displayName, String description, String colorCode) {
        this.displayName = displayName;
        this.description = description;
        this.colorCode = colorCode;
    }
    
    /**
     * 判断是否比另一个风险等级高
     */
    public boolean isHigherThan(RiskLevel other) {
        return this.ordinal() > other.ordinal();
    }
    
    /**
     * 判断是否为高风险
     */
    public boolean isHighRisk() {
        return this == HIGH || this == CRITICAL;
    }
}