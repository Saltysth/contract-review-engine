package com.contractreview.reviewengine.interfaces.rest.mapper;

import com.contractreview.reviewengine.domain.model.Task;
import com.contractreview.reviewengine.interfaces.rest.dto.TaskDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
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
    @Mapping(target = "id", source = "id", qualifiedByName = "baseTaskIdToString")
    @Mapping(target = "createdTime", source = "auditInfo.createdTime")
    @Mapping(target = "updatedTime", source = "auditInfo.updatedTime")
    TaskDto toDto(Task task);
    
    /**
     * DTO转Domain对象
     */
    @Mapping(target = "id", source = "id", qualifiedByName = "baseStringToTaskId")
    @Mapping(target = "auditInfo", ignore = true)
    Task toEntity(TaskDto dto);
    
    /**
     * TaskId转字符串 - Task专用
     */
    @Named("baseTaskIdToString")
    default String baseTaskIdToString(Long taskId) {
        return taskId != null ? taskId.toString() : null;
    }
    
    /**
     * 字符串转TaskId - Task专用
     */
    @Named("baseStringToTaskId")
    default Long baseStringToTaskId(String id) {
        return id != null ? Long.valueOf(id) : null;
    }
}