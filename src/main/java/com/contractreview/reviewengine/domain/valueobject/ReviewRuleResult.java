package com.contractreview.reviewengine.domain.valueobject;

import com.contractreview.reviewengine.domain.enums.RiskLevel;
import org.springframework.lang.Nullable;
import lombok.Builder;
import lombok.Value;

import java.util.List;

/**
 * 审查规则级别的结果
 * 
 * @author SaltyFish
 */
@Value
@Builder
public class ReviewRuleResult {

    /**
     * 风险名称
     */
    String riskName;

    /**
     * 属于对审查规则类型
     */
    String ruleType;

    /**
     * 0 无风险
     * [1~59] 低风险
     * [60~79] 中等风险
     * [80~89] 高风险
     * [90-100] 严重风险
     */
    RiskLevel riskLevel;

    Double riskScore;

    /**
     * 十文字以内概要
     */
    String summary;

    /**
     * 条款级别的结果
     */
    List<String> findings;

    /**
     * 操作建议
     */
    List<String> recommendation;

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