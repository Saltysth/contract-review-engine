package com.contractreview.reviewengine.domain.valueobject;

import com.contractreview.reviewengine.domain.enums.RiskLevel;
import org.springframework.lang.Nullable;
import lombok.Builder;
import lombok.Value;

/**
 * 风险对象
 * 
 * @author SaltyFish
 */
@Value
@Builder
public class RiskItem {
    
    String factorName;

    /**
     * 0 无风险
     * [1~59] 低风险
     * [60~79] 中等风险
     * [80~89] 高风险
     * [90-100] 严重风险
     */
    RiskLevel riskLevel;

    Double riskScore;

    Double confidence;

    /**
     * 十文字以内概要
     */
    String riskSummary;

    /**
     * 操作建议
     */
    String recommendation;

    /**
     * 风险条款Id
     */
    @Nullable
    Long riskClauseId;

    /**
     * 风险原文，尽在风险条款Id为空的情况下存在，即非条款风险
     */
    @Nullable
    String originContractText;
}