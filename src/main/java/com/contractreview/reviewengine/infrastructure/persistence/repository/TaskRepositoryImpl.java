package com.contractreview.reviewengine.infrastructure.persistence.repository;

import com.contractreview.reviewengine.domain.enums.TaskStatus;
import com.contractreview.reviewengine.domain.enums.TaskType;
import com.contractreview.reviewengine.domain.model.Task;
import com.contractreview.reviewengine.domain.model.TaskId;
import com.contractreview.reviewengine.domain.repository.TaskRepository;
import com.contractreview.reviewengine.infrastructure.persistence.converter.TaskConverter;
import com.contractreview.reviewengine.infrastructure.persistence.entity.TaskEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 任务仓储实现
 *
 * @author SaltyFish
 */
@Repository
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TaskRepositoryImpl implements TaskRepository {

    private final TaskJpaRepository jpaRepository;
    private final TaskConverter converter;

    @Override
    public Task save(Task task) {
        if (task == null) {
            throw new IllegalArgumentException("Task cannot be null");
        }

        TaskEntity entity = converter.toEntity(task);
        TaskEntity savedEntity = jpaRepository.save(entity);
        return converter.toDomain(savedEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Task> findById(TaskId taskId) {
        if (taskId == null) {
            return Optional.empty();
        }

        TaskEntity entity = jpaRepository.findByTaskId(taskId.getValue());
        if (entity == null) {
            return Optional.empty();
        }
        return Optional.of(converter.toDomain(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Task> findByStatus(TaskStatus status) {
        if (status == null) {
            return List.of();
        }

        List<TaskEntity> entities = jpaRepository.findByStatus(status);
        return converter.toDomainList(entities);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Task> findByTaskType(TaskType taskType) {
        if (taskType == null) {
            return List.of();
        }

        List<TaskEntity> entities = jpaRepository.findByTaskType(taskType);
        return converter.toDomainList(entities);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Task> findByStatusAndTaskType(TaskStatus status, TaskType taskType) {
        if (status == null || taskType == null) {
            return List.of();
        }

        List<TaskEntity> entities = jpaRepository.findByStatusAndTaskType(status, taskType);
        return converter.toDomainList(entities);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Task> findByCreatedTimeBetween(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null) {
            return List.of();
        }

        List<TaskEntity> entities = jpaRepository.findByCreatedTimeBetween(startTime, endTime);
        return converter.toDomainList(entities);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Task> findTimeoutTasks(TaskStatus status, LocalDateTime timeoutThreshold) {
        if (status == null || timeoutThreshold == null) {
            return List.of();
        }

        List<TaskEntity> entities = jpaRepository.findTimeoutTasks(status, timeoutThreshold);
        return converter.toDomainList(entities);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Task> findByStatusOrderByCreatedTimeDesc(TaskStatus status, Pageable pageable) {
        if (status == null || pageable == null) {
            return Page.empty();
        }

        Page<TaskEntity> entityPage = jpaRepository.findByStatusOrderByCreatedTimeDesc(status, pageable);
        return entityPage.map(converter::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Object[]> countByStatus() {
        return jpaRepository.countByStatus();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(TaskId taskId) {
        if (taskId == null) {
            return false;
        }

        return jpaRepository.existsByTaskId(taskId.getValue());
    }

    @Override
    public void deleteById(TaskId taskId) {
        if (taskId == null) {
            throw new IllegalArgumentException("TaskId cannot be null");
        }

        if (!existsById(taskId)) {
            throw new IllegalArgumentException("Task not found with id: " + taskId);
        }

        jpaRepository.deleteByTaskId(taskId.getValue());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Task> findAll() {
        List<TaskEntity> entities = jpaRepository.findAll();
        return converter.toDomainList(entities);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Task> findAllById(Iterable<TaskId> taskIds) {
        if (taskIds == null) {
            return List.of();
        }

        List<Long> taskIdValues = new java.util.ArrayList<>();
        for (TaskId taskId : taskIds) {
            taskIdValues.add(taskId.getValue());
        }

        List<TaskEntity> entities = jpaRepository.findAllById(taskIdValues);
        return converter.toDomainList(entities);
    }

    @Override
    public void deleteAllById(Iterable<TaskId> taskIds) {
        if (taskIds == null) {
            return;
        }

        for (TaskId taskId : taskIds) {
            deleteById(taskId);
        }
    }

    @Override
    public void deleteAll() {
        jpaRepository.deleteAll();
    }
}