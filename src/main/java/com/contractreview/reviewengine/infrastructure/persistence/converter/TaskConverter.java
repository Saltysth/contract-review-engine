package com.contractreview.reviewengine.infrastructure.persistence.converter;

import com.contractreview.reviewengine.domain.model.Task;
import com.contractreview.reviewengine.domain.model.TaskId;
import com.contractreview.reviewengine.domain.valueobject.AuditInfo;
import com.contractreview.reviewengine.infrastructure.persistence.entity.TaskEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 任务实体转换器 - 用于领域实体和基础设施实体之间的转换
 *
 * @author SaltyFish
 */
@Component
public class TaskConverter {

    /**
     * 将领域实体转换为基础设施实体
     */
    public TaskEntity toEntity(Task domain) {
        if (domain == null) {
            return null;
        }

        AuditInfo auditInfo = domain.getAuditInfo();

        return TaskEntity.builder()
                .id(domain.getId() != null ? domain.getId().getValue() : null)
                .taskName(domain.getTaskName())
                .taskType(domain.getTaskType())
                .status(domain.getStatus())
                .currentStage(domain.getCurrentStage())
                .configuration(domain.getConfiguration())
                .errorMessage(domain.getErrorMessage())
                .startTime(domain.getStartTime())
                .endTime(domain.getCompletedAt())
                .createdBy(auditInfo != null ? auditInfo.getCreatedBy() : null)
                .updatedBy(auditInfo != null ? auditInfo.getUpdatedBy() : null)
                .createdTime(auditInfo != null ? auditInfo.getCreatedTime() : null)
                .updatedTime(auditInfo != null ? auditInfo.getUpdatedTime() : null)
                .objectVersionNumber(auditInfo != null ? auditInfo.getObjectVersionNumber() : null)
                .build();
    }

    /**
     * 将基础设施实体转换为领域实体
     */
    public Task toDomain(TaskEntity entity) {
        if (entity == null) {
            return null;
        }

        AuditInfo auditInfo = new AuditInfo(
                entity.getCreatedBy(),
                entity.getCreatedTime(),
                entity.getUpdatedBy(),
                entity.getUpdatedTime(),
                entity.getObjectVersionNumber()
        );

        return Task.reconstruct(
                TaskId.of(entity.getId()),
                entity.getTaskName(),
                entity.getTaskType(),
                entity.getStatus(),
                entity.getCurrentStage(),
                entity.getConfiguration(),
                entity.getErrorMessage(),
                entity.getStartTime(),
                entity.getEndTime(),
                auditInfo
        );
    }

    /**
     * 将基础设施实体列表转换为领域实体列表
     */
    public List<Task> toDomainList(List<TaskEntity> entities) {
        if (entities == null) {
            return List.of();
        }

        return entities.stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }
}