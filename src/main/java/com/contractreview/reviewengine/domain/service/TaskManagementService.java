package com.contractreview.reviewengine.domain.service;

import com.contractreview.reviewengine.domain.enums.ExecutionStage;
import com.contractreview.reviewengine.domain.enums.TaskStatus;
import com.contractreview.reviewengine.domain.enums.TaskType;
import com.contractreview.reviewengine.domain.model.Task;
import com.contractreview.reviewengine.domain.model.TaskId;
import com.contractreview.reviewengine.domain.repository.TaskRepository;
import com.contractreview.reviewengine.domain.valueobject.TaskConfiguration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 任务管理领域服务
 * 负责处理任务的生命周期管理、状态转换等业务逻辑
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TaskManagementService {

    private final TaskRepository taskRepository;

    /**
     * 创建新任务
     */
    public Task createTask(TaskType taskType, TaskConfiguration configuration) {
        String taskName = "Contract Review Task - " + taskType;
        Task task = Task.create(taskName, taskType, 1L); // TODO: 从安全上下文获取当前用户ID
        task.updateCurrentStage(ExecutionStage.CLAUSE_EXTRACTION);

        if (configuration != null) {
            task.updateConfiguration(configuration);
        }

        Task savedTask = taskRepository.save(task);
        log.info("Created new task: {}", savedTask.getId());

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
    public void retryTask(TaskId taskId) {
        Task task = getTaskById(taskId);

        if (task.canRetry()) {
            task.retry();
            taskRepository.save(task);
            log.info("Retrying task: {} (attempt {})", taskId, task.getConfiguration().getRetryPolicy().getRetryCount());
        } else {
            log.warn("Task cannot be retried: {} (max retries exceeded)", taskId);
            throw new IllegalStateException("Task cannot be retried: max retries exceeded");
        }
    }

    /**
     * 删除任务
     */
    public boolean deleteTask(TaskId taskId) {
        try {
            taskRepository.deleteById(taskId);
            log.info("Deleted task: {}", taskId);
            return true;
        } catch (Exception e) {
            log.error("Failed to delete task: {}", taskId, e);
            return false;
        }
    }

    /**
     * 根据ID获取任务
     */
    public Task getTaskById(TaskId taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found: " + taskId));
    }

    /**
     * 根据状态获取任务列表
     */
    public List<Task> getTasksByStatus(TaskStatus status) {
        return taskRepository.findByStatus(status);
    }

    /**
     * 查找超时任务
     */
    public List<Task> findTimeoutTasks() {
        return taskRepository.findTimeoutTasks(TaskStatus.RUNNING, java.time.LocalDateTime.now().minusMinutes(30));
    }

    /**
     * 检查任务是否可以重试
     */
    public boolean canRetryTask(TaskId taskId) {
        Task task = getTaskById(taskId);
        return task.canRetry();
    }

    /**
     * 检查任务是否超时
     */
    public boolean isTaskTimeout(TaskId taskId) {
        Task task = getTaskById(taskId);
        return task.isTimeout();
    }

    /**
     * 获取任务状态
     */
    public TaskStatus getTaskStatus(TaskId taskId) {
        Task task = getTaskById(taskId);
        return task.getStatus();
    }

    /**
     * 获取任务配置
     */
    public TaskConfiguration getTaskConfiguration(TaskId taskId) {
        Task task = getTaskById(taskId);
        return task.getConfiguration();
    }

    /**
     * 更新任务配置
     */
    public void updateTaskConfiguration(TaskId taskId, TaskConfiguration configuration) {
        Task task = getTaskById(taskId);
        task.updateConfiguration(configuration);
        taskRepository.save(task);
        log.info("Updated task configuration for task: {}", taskId);
    }
}