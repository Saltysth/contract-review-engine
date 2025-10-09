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
    @Mapping(target = "createdTime", source = "createdTime")
    @Mapping(target = "summary", source = "summary")
    ReviewResultDto toDto(ReviewResult reviewResult);
    
    /**
     * DTO转Domain对象
     */
    @Mapping(target = "taskId", source = "taskId", qualifiedByName = "stringToTaskId")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "reviewType", ignore = true)
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

}