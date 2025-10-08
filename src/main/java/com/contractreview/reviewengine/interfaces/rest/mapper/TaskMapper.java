package com.contractreview.reviewengine.interfaces.rest.mapper;

import com.contractreview.reviewengine.domain.model.Task;
import com.contractreview.reviewengine.domain.model.TaskId;
import com.contractreview.reviewengine.interfaces.rest.dto.TaskDto;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

/**
 * 基础任务映射器
 */
@Mapper(config = GlobalMappingConfig.class)
public interface TaskMapper {
    
    TaskMapper INSTANCE = Mappers.getMapper(TaskMapper.class);
    
    /**
     * Domain对象转DTO
     */
    @Mapping(target = "id", source = "id", qualifiedByName = "taskIdToString")
    @Mapping(target = "createdAt", source = "auditInfo.createdAt")
    @Mapping(target = "updatedAt", source = "auditInfo.updatedAt")
    TaskDto toDto(Task task);
    
    /**
     * DTO转Domain对象
     */
    @Mapping(target = "id", source = "id", qualifiedByName = "stringToTaskId")
    @Mapping(target = "auditInfo", ignore = true)
    Task toEntity(TaskDto dto);
    
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