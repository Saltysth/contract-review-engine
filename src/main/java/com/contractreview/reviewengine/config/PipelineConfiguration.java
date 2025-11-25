package com.contractreview.reviewengine.config;

import com.contractreview.reviewengine.infrastructure.pipeline.PipelineStage;
import com.contractreview.reviewengine.infrastructure.pipeline.stages.ExtractionStage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 管道配置
 * @deprecated 此配置类已被弃用。管道架构将被简化的直接处理方式替代。
 * 该配置在未来的版本中将被移除。
 * 迁移指南：此配置类中的Bean定义将被移除，相关功能将整合到 {@link com.contractreview.reviewengine.application.service.ContractReviewService} 中。
 * @since 1.0.0
 * @see com.contractreview.reviewengine.application.service.ContractReviewService
 */
@Configuration
@Deprecated(since = "1.0.0", forRemoval = true)
public class PipelineConfiguration {
    
    /**
     * 配置管道阶段执行顺序
     */
    @Bean
    public List<PipelineStage> pipelineStages(
            ExtractionStage extractionStage) {
        
        return List.of(
                extractionStage
        );
    }
}