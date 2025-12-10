package com.contractreview.reviewengine.application.service;

import com.contract.common.dto.DeleteClauseExtractionResponse;
import com.contract.common.feign.ClauseExtractionFeignClient;
import com.contractreview.reviewengine.domain.enums.TaskStatus;
import com.contractreview.reviewengine.domain.enums.TaskType;
import com.contractreview.reviewengine.domain.model.ContractReview;
import com.contractreview.reviewengine.domain.model.Task;
import com.contractreview.reviewengine.domain.model.TaskId;
import com.contractreview.reviewengine.domain.repository.TaskRepository;
import com.contractreview.reviewengine.domain.valueobject.TaskConfiguration;
import com.contractreview.reviewengine.infrastructure.service.ContractTaskInfraService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 任务应用服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TaskService {

    private final TaskRepository taskRepository;
    private final ContractTaskInfraService contractTaskInfraService;
    private final ClauseExtractionFeignClient clauseExtractionFeignClient;

    /**
     * 创建新任务
     */
    public Task createTask(String taskName, TaskType taskType, Long createdBy) {
        Task task = Task.create(taskName, taskType, createdBy);

        Task savedTask = taskRepository.save(task);
        log.info("Created new task: {} with name: {}", savedTask.getId(), taskName);

        return savedTask;
    }

    /**
     * 启动任务
     */
    public void startTask(TaskId taskId) {
        Task task = getTaskById(taskId);
        task.start();
        taskRepository.save(task);

        log.info("Started task: {}", taskId);
    }

    /**
     * 完成任务
     */
    public void completeTask(TaskId taskId) {
        Task task = getTaskById(taskId);
        task.complete();
        taskRepository.save(task);

        log.info("Completed task: {}", taskId);
    }

    /**
     * 任务失败
     */
    public void failTask(TaskId taskId, String errorMessage) {
        Task task = getTaskById(taskId);
        task.fail(errorMessage);
        taskRepository.save(task);

        log.warn("Task failed: {} - {}", taskId, errorMessage);
    }

    /**
     * 取消任务
     */
    public void cancelTask(TaskId taskId) {
        Task task = getTaskById(taskId);
        task.cancel();
        taskRepository.save(task);

        log.info("Cancelled task: {}", taskId);
    }

    /**
     * 重试任务
     */
    public void retryTask(Task task) {
        // 不仅要重制任务状态，还需要把相关步骤的生成结果全部软删除。
        if (task.canRetry()) {
            task.retry();
            taskRepository.save(task);
        } else {
            log.warn("Task cannot be retried: {} (max retries exceeded)", task.getId());
            throw new IllegalStateException("Task cannot be retried: max retries exceeded");
        }
    }

    /**
     * 根据ID获取任务
     */
    @Transactional(readOnly = true)
    public Task getTaskById(TaskId taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found: " + taskId));
    }

    /**
     * 根据状态获取任务列表
     */
    @Transactional(readOnly = true)
    public List<Task> getTasksByStatus(TaskStatus status) {
        return taskRepository.findByStatus(status);
    }

    /**
     * 分页获取任务
     */
    @Transactional(readOnly = true)
    public Page<Task> getTasksByStatus(TaskStatus status, Pageable pageable) {
        return taskRepository.findByStatusOrderByCreatedTimeDesc(status, pageable);
    }

    /**
     * 查找超时任务
     */
    @Transactional(readOnly = true)
    public List<Task> findTimeoutTasks() {
        LocalDateTime timeoutThreshold = LocalDateTime.now().minusMinutes(30); // 30分钟超时
        return taskRepository.findTimeoutTasks(TaskStatus.RUNNING, timeoutThreshold);
    }

    /**
     * 查找可重试的失败任务
     */
    @Transactional(readOnly = true)
    public List<Task> findRetryableTasks() {
        List<Task> failedTasks = taskRepository.findByStatus(TaskStatus.FAILED);
        return failedTasks.stream()
                .filter(Task::canRetry)
                .toList();
    }

    /**
     * 获取任务统计信息
     */
    @Transactional(readOnly = true)
    public List<Object[]> getTaskStatistics() {
        return taskRepository.countByStatus();
    }

    /**
     * 删除任务
     */
    public void deleteTask(TaskId taskId) {
        if (!taskRepository.existsById(taskId)) {
            throw new IllegalArgumentException("Task not found: " + taskId);
        }

        taskRepository.deleteById(taskId);
        log.info("Deleted task: {}", taskId);
    }

    /**
     * 更新任务配置
     */
    public void updateTaskConfiguration(TaskId taskId, TaskConfiguration configuration) {
        Task task = getTaskById(taskId);
        task.updateConfiguration(configuration);
        taskRepository.save(task);

        log.info("Updated configuration for task: {}", taskId);
    }

    /**
     * 更新任务当前阶段
     * @deprecated 此方法已被弃用。管道阶段处理方式将被简化的直接处理方式替代。
     * 请使用任务状态变更来跟踪任务进度。
     * 迁移指南：使用 {@link #startTask(TaskId)}、{@link #completeTask(TaskId)}、{@link #failTask(TaskId, String)} 来管理任务状态。
     * @since 1.0.0
     * @see #startTask(TaskId)
     * @see #completeTask(TaskId)
     * @see #failTask(TaskId, String)
     */
    @Deprecated(since = "1.0.0", forRemoval = true)
    public void updateTaskStage(TaskId taskId, com.contractreview.reviewengine.domain.enums.ExecutionStage stage) {
        Task task = getTaskById(taskId);
        task.updateCurrentStage(stage);
        taskRepository.save(task);

        log.info("Updated stage for task: {} to {}", taskId, stage);
    }

    /**
     * 检查任务超时并处理
     */
    public void checkAndHandleTimeoutTasks() {
        List<Task> timeoutTasks = findTimeoutTasks();
        for (Task task : timeoutTasks) {
            if (task.isTimeout()) {
                task.fail("Task timeout");
                taskRepository.save(task);
                log.warn("Task {} timed out and marked as failed", task.getId());
            }
        }
    }

    /**
     * 获取所有任务
     */
    @Transactional(readOnly = true)
    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    /**
     * 根据任务类型获取任务
     */
    @Transactional(readOnly = true)
    public List<Task> getTasksByType(TaskType taskType) {
        return taskRepository.findByTaskType(taskType);
    }

    /**
     * 根据状态和类型获取任务
     */
    @Transactional(readOnly = true)
    public List<Task> getTasksByStatusAndType(TaskStatus status, TaskType taskType) {
        return taskRepository.findByStatusAndTaskType(status, taskType);
    }

    /**
     * 检查任务名称是否存在
     */
    @Transactional(readOnly = true)
    public boolean existsByTaskName(String taskName) {
        return taskRepository.existsByTaskName(taskName);
    }
}