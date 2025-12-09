package com.contractreview.reviewengine.domain.valueobject;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 关键点信息
 * 包含条款级别的关键发现和对应的修复建议
 *
 * @author SaltyFish
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KeyPoint {

    /**
     * 关键点描述
     * 注重于条款的具体问题点
     */
    private String point;

    /**
     * 关键点类型
     */
    private String type;

    /**
     * 针对该关键点的修复建议列表
     */
    private List<String> remediationSuggestions;

    /**
     * 风险等级
     */
    private String riskLevel;

    /**
     * 相关的审查规则ID
     */
    private Long reviewRuleId;

    /**
     * 相关的条款ID列表，逗号分隔
     */
    private String clauseIds;
}