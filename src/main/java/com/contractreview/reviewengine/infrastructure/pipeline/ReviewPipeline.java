package com.contractreview.reviewengine.infrastructure.pipeline;

import com.contractreview.reviewengine.domain.enums.ExecutionStage;
import com.contractreview.reviewengine.domain.model.ContractReview;
import com.contractreview.reviewengine.domain.model.ReviewResult;
import com.contractreview.reviewengine.domain.valueobject.StageResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 审查管道主控制器
 * @deprecated 此类已被弃用。请使用 {@link com.contractreview.reviewengine.application.service.ContractReviewService} 中的简化方法进行合同审查。
 * 该管道架构过于复杂且未完全实现，将被替换为直接的合同审查处理方式。
 * 迁移指南：使用 {@code startContractReviewDirect()} 方法替代多阶段管道处理。
 * @since 1.0.0
 * @see com.contractreview.reviewengine.application.service.ContractReviewService#startContractReviewDirect
 */
@Component
@RequiredArgsConstructor
@Slf4j
@Deprecated(since = "1.0.0", forRemoval = true)
public class ReviewPipeline {
    
    private final List<PipelineStage> stages;
    
    /**
     * 执行完整的审查管道
     */
    public ReviewResult execute(ContractReview contractReview) {
        log.info("Starting review pipeline for task: {}", contractReview.getId());

        ReviewResult.ReviewResultBuilder resultBuilder = ReviewResult.builder()
                .taskId(contractReview.getId());
        
        ExecutionStage currentStage = ExecutionStage.CLAUSE_EXTRACTION; // TODO
        
        try {
            for (PipelineStage stage : stages) {
                if (stage.getStage() == currentStage) {
                    log.info("Executing stage: {} for task: {}", currentStage, contractReview.getId());

                    StageResult stageResult = stage.execute(contractReview, resultBuilder.build());

                    if (!stageResult.isSuccess()) {
                        log.error("Stage {} failed for task: {} - {}",
                                currentStage, contractReview.getId(), stageResult.getErrorMessage());
                        
                        return resultBuilder
                                .build();
                    }
                    
                    // 更新到下一个阶段
                    currentStage = getNextStage(currentStage);
                    if (currentStage == null) {
                        break; // 所有阶段完成
                    }
                }
            }
            
            ReviewResult result = resultBuilder
                    .build();

            log.info("Review pipeline completed successfully for task: {}", contractReview.getId());
            return result;

        } catch (Exception e) {
            log.error("Review pipeline failed for task: {}", contractReview.getId(), e);

            return resultBuilder
                    .build();
        }
    }
    
    /**
     * 获取下一个执行阶段
     */
    private ExecutionStage getNextStage(ExecutionStage currentStage) {
        return switch (currentStage) {
//            case PARSING -> ExecutionStage.EXTRACTION;
//            case EXTRACTION -> ExecutionStage.ANALYSIS;
//            case ANALYSIS -> ExecutionStage.RISK_ASSESSMENT;
//            case RISK_ASSESSMENT -> null; // 完成
            default -> null;
        };
    }
}