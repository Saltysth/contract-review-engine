package com.contractreview.reviewengine.domain.enums;

import lombok.Getter;

/**
 * 执行阶段枚举
 *
 * @author SaltyFish
 * @deprecated 此枚举已被弃用。管道阶段处理方式将被简化的直接处理方式替代。
 * 此枚举定义的多阶段管道执行流程将被替换为简单的任务状态管理。
 * 迁移指南：使用 {@link TaskStatus} 枚举来管理任务状态，替代多阶段执行流程。
 * @since 1.0.0
 * @see com.contractreview.reviewengine.domain.enums.TaskStatus
 */
@Getter
@Deprecated(since = "1.0.0", forRemoval = true)
public enum ExecutionStage {

    CONTRACT_CLASSIFICATION("合同分类"),
    CLAUSE_EXTRACTION("条款抽取"),
    KNOWLEDGE_MATCHING("知识库匹配"),
    RULE_MATCHING("规则匹配"),
    MODEL_REVIEW("模型审查"),
    RESULT_VALIDATION("结果校验"),
    REPORT_GENERATION("报告生成"),
    REVIEW_COMPLETED("审查完毕");
    
    private final String displayName;

    ExecutionStage(String displayName) {
        this.displayName = displayName;
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
        return this == REVIEW_COMPLETED;
    }
}