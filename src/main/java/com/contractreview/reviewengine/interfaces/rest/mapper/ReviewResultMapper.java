package com.contractreview.reviewengine.interfaces.rest.mapper;

import com.contractreview.reviewengine.domain.model.ReviewResult;
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
    @Mapping(target = "taskId", source = "taskId", qualifiedByName = "reviewResultTaskIdToString")
    @Mapping(target = "createdTime", source = "createdTime")
    @Mapping(target = "summary", source = "summary")
    @Mapping(target = "recommendations", source = "recommendations")
    @Mapping(target = "currentStage", ignore = true)
    @Mapping(target = "riskAssessment", ignore = true)
    @Mapping(target = "extractedClauses", ignore = true)
    @Mapping(target = "analysisResult", ignore = true)
    @Mapping(target = "auditInfo", ignore = true)
    @Mapping(target = "updatedTime", ignore = true)
    ReviewResultDto toDto(ReviewResult reviewResult);
    
    /**
     * DTO转Domain对象
     */
    @Mapping(target = "taskId", source = "taskId", qualifiedByName = "reviewResultStringToTaskId")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "contractId", ignore = true)
    @Mapping(target = "reviewType", ignore = true)
    @Mapping(target = "overallRiskLevel", ignore = true)
    @Mapping(target = "riskScore", ignore = true)
    @Mapping(target = "complianceScore", ignore = true)
    @Mapping(target = "extractedEntities", ignore = true)
    @Mapping(target = "riskItems", ignore = true)
    @Mapping(target = "complianceIssues", ignore = true)
    ReviewResult toEntity(ReviewResultDto dto);
    
    /**
     * TaskId转字符串 - ReviewResult专用
     */
    @Named("reviewResultTaskIdToString")
    default String reviewResultTaskIdToString(Long taskId) {
        return taskId != null ? taskId.toString() : null;
    }
    
    /**
     * 字符串转TaskId - ReviewResult专用
     */
    @Named("reviewResultStringToTaskId")
    default Long reviewResultStringToTaskId(String taskId) {
        return taskId != null ? Long.valueOf(taskId) : null;
    }

}