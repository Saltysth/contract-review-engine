package com.contractreview.reviewengine.config;

import com.contractreview.reviewengine.infrastructure.pipeline.PipelineStage;
import com.contractreview.reviewengine.infrastructure.pipeline.stages.AnalysisStage;
import com.contractreview.reviewengine.infrastructure.pipeline.stages.ExtractionStage;
import com.contractreview.reviewengine.infrastructure.pipeline.stages.ParsingStage;
import com.contractreview.reviewengine.infrastructure.pipeline.stages.RiskAssessmentStage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 管道配置
 */
@Configuration
public class PipelineConfiguration {
    
    /**
     * 配置管道阶段执行顺序
     */
    @Bean
    public List<PipelineStage> pipelineStages(
            ParsingStage parsingStage,
            ExtractionStage extractionStage,
            AnalysisStage analysisStage,
            RiskAssessmentStage riskAssessmentStage) {
        
        return List.of(
                parsingStage,
                extractionStage,
                analysisStage,
                riskAssessmentStage
        );
    }
}