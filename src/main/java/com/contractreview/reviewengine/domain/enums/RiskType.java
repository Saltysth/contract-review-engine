package com.contractreview.reviewengine.domain.enums;

/**
 * 风险类型枚举
 *
 * @author SaltyFish
 */
public enum RiskType {
    /**
     * 风险
     */
    RISK("risk"),

    /**
     * 警告
     */
    WARNING("warning"),

    /**
     * 信息
     */
    INFO("info"),

    /**
     * 建议
     */
    SUGGESTION("suggestion");

    private final String value;

    RiskType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static RiskType fromValue(String value) {
        for (RiskType type : RiskType.values()) {
            if (type.getValue().equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown RiskType: " + value);
    }
}