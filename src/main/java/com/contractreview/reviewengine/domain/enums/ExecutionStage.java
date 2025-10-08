package com.contractreview.reviewengine.domain.enums;

import lombok.Getter;

/**
 * 执行阶段枚举
 * 
 * @author SaltyFish
 */
@Getter
public enum ExecutionStage {
    
    PENDING("待处理", "等待开始处理", 0),
    DOCUMENT_PARSING("文档解析", "解析合同文档内容", 10),
    CONTRACT_EXTRACTION("合同提取", "提取合同关键信息", 30),
    CONTENT_ANALYSIS("内容分析", "分析合同条款内容", 60),
    RISK_EVALUATION("风险评估", "评估合同风险等级", 80),
    RULE_EXECUTION("规则执行", "执行业务规则", 90),
    COMPLETED("已完成", "所有阶段执行完成", 100),
    FAILED("执行失败", "执行过程中出现错误", -1);
    
    private final String displayName;
    private final String description;
    private final int defaultProgress;
    
    ExecutionStage(String displayName, String description, int defaultProgress) {
        this.displayName = displayName;
        this.description = description;
        this.defaultProgress = defaultProgress;
    }
    
    /**
     * 获取下一个阶段
     */
    public ExecutionStage nextStage() {
        ExecutionStage[] stages = values();
        int currentIndex = this.ordinal();
        return currentIndex < stages.length - 1 ? stages[currentIndex + 1] : this;
    }
    
    /**
     * 是否为最终阶段
     */
    public boolean isFinalStage() {
        return this == COMPLETED;
    }
}