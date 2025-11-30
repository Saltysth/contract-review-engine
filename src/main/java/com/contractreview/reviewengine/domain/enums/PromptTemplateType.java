package com.contractreview.reviewengine.domain.enums;

import lombok.Getter;

/**
 * 审查类型枚举
 * 
 * @author SaltyFish
 */
@Getter
public enum PromptTemplateType {
    // 较快速度 较低质量
    EFFICIENT("效率模板", "快速审查模式，关注关键风险点", (short) 0),
    // 正常速度 正常质量
    STANDARD("标准模板", "适用于大多数商业合同的通用审查模板", (short) 1),
    // 较慢速度 较高质量
    STRICT("严格模板", "更严格的风险识别标准，适用于高风险合同", (short) 2);

    private final String displayName;
    private final String description;
    private final Short code;

    PromptTemplateType(String displayName, String description, Short code) {
        this.displayName = displayName;
        this.description = description;
        this.code = code;
    }

}