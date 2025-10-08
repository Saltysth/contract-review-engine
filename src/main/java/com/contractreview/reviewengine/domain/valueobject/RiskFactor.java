package com.contractreview.reviewengine.domain.valueobject;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

/**
 * 风险因子值对象
 * 
 * @author SaltyFish
 */
@Value
@Builder
public class RiskFactor {
    
    String factorName;
    com.contractreview.reviewengine.domain.enums.RiskLevel riskLevel;
    Double score;
    String description;
    String recommendation;
    String category;
    
    /**
     * 获取风险类型（兼容性方法）
     */
    public String getType() {
        return this.category != null ? this.category : this.factorName;
    }
    
    /**
     * 检查是否为高风险
     */
    public boolean isHighRisk() {
        return score != null && score >= 70.0;
    }
}