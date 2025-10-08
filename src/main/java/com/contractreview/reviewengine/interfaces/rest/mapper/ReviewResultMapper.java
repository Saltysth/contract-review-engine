package com.contractreview.reviewengine.interfaces.rest.mapper;

import com.contractreview.reviewengine.domain.model.ReviewResult;
import com.contractreview.reviewengine.domain.model.TaskId;
import com.contractreview.reviewengine.interfaces.rest.dto.ReviewResultDto;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

/**
 * 审查结果映射器
 */
@Mapper(config = GlobalMappingConfig.class)
public interface ReviewResultMapper {
    
    ReviewResultMapper INSTANCE = Mappers.getMapper(ReviewResultMapper.class);
    
    /**
     * Domain对象转DTO
     */
    @Mapping(target = "taskId", source = "taskId", qualifiedByName = "taskIdToString")
    @Mapping(target = "createdAt", source = "auditInfo.createdAt")
    @Mapping(target = "updatedAt", source = "auditInfo.updatedAt")
    @Mapping(target = "summary", expression = "java(generateSummary(reviewResult))")
    ReviewResultDto toDto(ReviewResult reviewResult);
    
    /**
     * DTO转Domain对象
     */
    @Mapping(target = "taskId", source = "taskId", qualifiedByName = "stringToTaskId")
    @Mapping(target = "auditInfo", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "reviewType", ignore = true)
    @Mapping(target = "confidenceScore", ignore = true)
    @Mapping(target = "processingTimeMs", ignore = true)
    ReviewResult toEntity(ReviewResultDto dto);
    
    /**
     * TaskId转字符串
     */
    @Named("taskIdToString")
    default String taskIdToString(Long taskId) {
        return taskId != null ? taskId.toString() : null;
    }
    
    /**
     * 字符串转TaskId
     */
    @Named("stringToTaskId")
    default Long stringToTaskId(String id) {
        return id != null ? Long.valueOf(id) : null;
    }
    
    /**
     * 生成审查摘要
     */
    default String generateSummary(ReviewResult reviewResult) {
        if (reviewResult == null) {
            return null;
        }
        
        // 根据风险评估生成摘要
        if (reviewResult.getRiskAssessment() != null) {
            String riskLevel = reviewResult.getRiskAssessment().getOverallRiskLevel().name();
            return String.format("审查完成，整体风险等级: %s", riskLevel);
        }
        
        return "审查完成";
    }
}