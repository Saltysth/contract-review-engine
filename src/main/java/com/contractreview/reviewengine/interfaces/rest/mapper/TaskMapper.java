package com.contractreview.reviewengine.interfaces.rest.mapper;

import com.contractreview.reviewengine.domain.model.Task;
import com.contractreview.reviewengine.domain.model.TaskId;
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
    @Mapping(target = "id", source = "id", qualifiedByName = "taskIdToString")
    @Mapping(target = "startTime", source = "startTime")
    @Mapping(target = "completedAt", source = "completedAt")
    @Mapping(target = "progress", ignore = true)  // Progress is not part of Task domain entity
    TaskDto toDto(Task task);


    /**
     * TaskId转字符串
     */
    @Named("taskIdToString")
    default String taskIdToString(TaskId taskId) {
        return taskId != null ? taskId.getValue().toString() : null;
    }

    /**
     * 字符串转TaskId
     */
    @Named("stringToTaskId")
    default TaskId stringToTaskId(String id) {
        if (id == null || id.trim().isEmpty()) {
            return null;
        }
        try {
            return TaskId.of(Long.valueOf(id));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid TaskId format: " + id, e);
        }
    }
}