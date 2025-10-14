package com.contractreview.reviewengine.infrastructure.pipeline;

import com.contractreview.reviewengine.domain.enums.ExecutionStage;
import com.contractreview.reviewengine.domain.model.ContractReview;
import com.contractreview.reviewengine.domain.model.ReviewResult;
import com.contractreview.reviewengine.domain.valueobject.StageResult;

/**
 * 管道阶段接口
 */
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