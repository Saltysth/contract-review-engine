package com.contractreview.reviewengine.domain.enums;

import lombok.Getter;

/**
 * 审查类型枚举
 * 
 * @author SaltyFish
 */
@Getter
public enum PromptTemplateType {
    
    STANDARD("标准模板", "适用于大多数商业合同的通用审查模板"),
    STRICT("严格模板", "更严格的风险识别标准，适用于高风险合同"),
    EFFICIENT("效率模板", "快速审查模式，关注关键风险点");

    private final String displayName;
    private final String description;

    PromptTemplateType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

}