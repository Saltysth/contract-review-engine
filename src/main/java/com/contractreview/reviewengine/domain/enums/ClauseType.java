package com.contractreview.reviewengine.domain.enums;

import lombok.Getter;

/**
 * 条款类型枚举
 * 
 * @author SaltyFish
 */
@Getter
public enum ClauseType {
    
    PAYMENT("付款条款", "关于付款方式、时间、金额的条款"),
    DELIVERY("交付条款", "关于交付时间、地点、方式的条款"),
    WARRANTY("保证条款", "关于产品或服务保证的条款"),
    LIABILITY("责任条款", "关于责任承担和限制的条款"),
    TERMINATION("终止条款", "关于合同终止条件的条款"),
    DISPUTE_RESOLUTION("争议解决", "关于争议解决方式的条款"),
    CONFIDENTIALITY("保密条款", "关于保密义务的条款"),
    INTELLECTUAL_PROPERTY("知识产权", "关于知识产权归属的条款"),
    FORCE_MAJEURE("不可抗力", "关于不可抗力的条款"),
    OTHER("其他条款", "其他类型的条款");
    
    private final String displayName;
    private final String description;
    
    ClauseType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
}