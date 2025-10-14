package com.contractreview.reviewengine.application.service;

import com.contractreview.reviewengine.domain.enums.TaskStatus;
import com.contractreview.reviewengine.domain.enums.TaskType;
import com.contractreview.reviewengine.domain.model.Task;
import com.contractreview.reviewengine.domain.model.TaskId;
import com.contractreview.reviewengine.domain.repository.TaskRepository;
import com.contractreview.reviewengine.domain.valueobject.TaskConfiguration;
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
    
    /**
     * 创建新任务
     */
    public Task createTask(TaskType taskType, TaskConfiguration configuration) {
        Long taskId = System.currentTimeMillis(); // 简单的ID生成
        
        Task task = Task.builder()
                .id(taskId)
                .taskType(taskType)
                .status(TaskStatus.PENDING)
                .build();
        
        task.initializeAuditInfo();
        
        Task savedTask = taskRepository.save(task);
        log.info("Created new task: {}", String.valueOf(taskId));
        
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
    public void completeTask(Long taskId) {
        Task task = getTaskById(TaskId.of(taskId));
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
    public List<Task> getTasksByStatus(com.contractreview.reviewengine.domain.enums.TaskStatus status) {
        // 转换为枚举类型
        TaskStatus enumStatus = TaskStatus.valueOf(status.name());
        return taskRepository.findByStatus(enumStatus);
    }
    
    /**
     * 分页获取任务
     */
    @Transactional(readOnly = true)
    public Page<Task> getTasksByStatus(com.contractreview.reviewengine.domain.enums.TaskStatus status, Pageable pageable) {
        // 转换为枚举类型
        TaskStatus enumStatus = TaskStatus.valueOf(status.name());
        return taskRepository.findByStatusOrderByAuditInfoCreatedTimeDesc(enumStatus, pageable);
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
     * 查找可重试的失败任务 TODO
     */
    @Transactional(readOnly = true)
    public List<Task> findRetryableTasks() {
        return null;
    }
    
    /**
     * 获取任务统计信息
     */
    @Transactional(readOnly = true)
    public List<Object[]> getTaskStatistics() {
        return taskRepository.countByStatus();
    }
}