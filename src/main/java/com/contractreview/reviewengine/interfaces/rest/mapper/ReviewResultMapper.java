package com.contractreview.reviewengine.interfaces.rest.mapper;

import com.contractreview.reviewengine.domain.model.ReviewResult;
import com.contractreview.reviewengine.domain.valueobject.KeyPoint;
import com.contractreview.reviewengine.interfaces.rest.dto.ReviewResultDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

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
    @Mapping(target = "recommendations", source = "keyPoints", qualifiedByName = "keyPointsToString")
    @Mapping(target = "currentStage", ignore = true)
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
    @Mapping(target = "summary", source = "summary")
    @Mapping(target = "keyPoints", source = "recommendations", qualifiedByName = "stringToKeyPoints")
    @Mapping(target = "stageResult", ignore = true)
    @Mapping(target = "ruleResults", ignore = true)
    @Mapping(target = "modelVersion", ignore = true)
    @Mapping(target = "evidences", ignore = true)
    @Mapping(target = "createdTime", ignore = true)
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

    /**
     * KeyPoints列表转字符串
     */
    @Named("keyPointsToString")
    default String keyPointsToString(List<KeyPoint> keyPoints) {
        if (keyPoints == null || keyPoints.isEmpty()) {
            return null;
        }

        ObjectMapper mapper = new ObjectMapper();
        try {
            // 将KeyPoint列表转换为JSON字符串
            return mapper.writeValueAsString(keyPoints);
        } catch (JsonProcessingException e) {
            // 如果转换失败，返回简单的文本描述
            StringBuilder sb = new StringBuilder();
            for (KeyPoint keyPoint : keyPoints) {
                if (sb.length() > 0) sb.append("; ");
                sb.append(keyPoint.getPoint());
            }
            return sb.toString();
        }
    }

    /**
     * 字符串转KeyPoints列表
     */
    @Named("stringToKeyPoints")
    default List<KeyPoint> stringToKeyPoints(String recommendations) {
        if (recommendations == null || recommendations.trim().isEmpty()) {
            return null;
        }

        ObjectMapper mapper = new ObjectMapper();
        try {
            // 尝试从JSON字符串解析
            return mapper.readValue(recommendations,
                mapper.getTypeFactory().constructCollectionType(List.class, KeyPoint.class));
        } catch (JsonProcessingException e) {
            // 如果不是JSON格式，创建一个包含该字符串的KeyPoint
            KeyPoint keyPoint = KeyPoint.builder()
                .point(recommendations)
                .type("GENERAL")
                .remediationSuggestions(List.of())
                .build();
            return List.of(keyPoint);
        }
    }

}