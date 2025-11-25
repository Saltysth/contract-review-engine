package com.contractreview.reviewengine.infrastructure.pipeline;

import com.contractreview.reviewengine.domain.enums.ExecutionStage;
import com.contractreview.reviewengine.domain.model.ContractReview;
import com.contractreview.reviewengine.domain.model.ReviewResult;
import com.contractreview.reviewengine.domain.valueobject.StageResult;

/**
 * 管道阶段接口
 * @deprecated 此接口已被弃用。请使用 {@link com.contractreview.reviewengine.application.service.ContractReviewService} 中的简化方法进行合同审查。
 * 该管道架构过于复杂且未完全实现，将被替换为直接的合同审查处理方式。
 * 迁移指南：使用 {@code startContractReviewDirect()} 方法替代多阶段管道处理。
 * @since 1.0.0
 * @see com.contractreview.reviewengine.application.service.ContractReviewService#startContractReviewDirect
 */
@Deprecated(since = "1.0.0", forRemoval = true)
public interface PipelineStage {
    
    /**
     * 获取阶段类型
     */
    ExecutionStage getStage();
    
    /**
     * 执行阶段处理
     */
    StageResult execute(ContractReview contractReview, ReviewResult currentResult);

    /**
     * 检查是否支持该任务类型
     */
    boolean supports(ContractReview contractReview);
}