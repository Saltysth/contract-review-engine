package com.contractreview.reviewengine.infrastructure.pipeline;

import com.contractreview.reviewengine.domain.enums.ExecutionStage;
import com.contractreview.reviewengine.domain.model.ContractTask;
import com.contractreview.reviewengine.domain.model.ReviewResult;
import com.contractreview.reviewengine.domain.valueobject.StageResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 审查管道主控制器
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ReviewPipeline {
    
    private final List<PipelineStage> stages;
    
    /**
     * 执行完整的审查管道
     */
    public ReviewResult execute(ContractTask contractTask) {
        log.info("Starting review pipeline for task: {}", contractTask.getId());
        
        ReviewResult.ReviewResultBuilder resultBuilder = ReviewResult.builder()
                .taskId(contractTask.getId());
        
        ExecutionStage currentStage = ExecutionStage.RULE_EXECUTION;
        
        try {
            for (PipelineStage stage : stages) {
                if (stage.getStage() == currentStage) {
                    log.info("Executing stage: {} for task: {}", currentStage, contractTask.getId());
                    
                    StageResult stageResult = stage.execute(contractTask, resultBuilder.build());
                    
                    if (!stageResult.isSuccess()) {
                        log.error("Stage {} failed for task: {} - {}", 
                                currentStage, contractTask.getId(), stageResult.getErrorMessage());
                        
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
            

            log.info("Review pipeline completed successfully for task: {}", contractTask.getId());
            return result;
            
        } catch (Exception e) {
            log.error("Review pipeline failed for task: {}", contractTask.getId(), e);
            
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