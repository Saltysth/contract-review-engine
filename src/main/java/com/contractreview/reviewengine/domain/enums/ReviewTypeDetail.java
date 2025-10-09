package com.contractreview.reviewengine.domain.enums;

import lombok.Getter;

/**
 * @author wangcong@SaltyFish
 * @description
 * @since 2025/10/09 19:42
 */
@Getter
public enum ReviewTypeDetail {

    RISK_ASSESSMENT("风险评估", "识别潜在法律和商业风险"),
    CLAUSE_ANALYSIS("条款分析", "分析关键条款的合理性"),
    COMPLETENESS_CHECK("完整性检查", "检查必要条款是否齐全"),
    COMPLIANCE_CHECK("合规检查", "检查是否符合相关法律法规"),
    PRECEDENT_COMPARISON("先例对比", "与类似合同条款对比");

    private final String displayName;
    private final String description;

    ReviewTypeDetail(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

}
