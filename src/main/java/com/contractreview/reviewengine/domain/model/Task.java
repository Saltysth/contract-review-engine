package com.contractreview.reviewengine.domain.model;

import com.contractreview.reviewengine.domain.enums.ExecutionStage;
import com.contractreview.reviewengine.domain.enums.TaskStatus;
import com.contractreview.reviewengine.domain.enums.TaskType;
import com.contractreview.reviewengine.domain.valueobject.AuditInfo;
import com.contractreview.reviewengine.domain.valueobject.TaskConfiguration;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * 任务聚合根 - 纯领域实体
 *
 * @author SaltyFish
 */
@Getter
@ToString
@EqualsAndHashCode(of = {"id"})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Task {

    private TaskId id;
    @Setter
    private String taskName;
    private TaskType taskType;
    private TaskStatus status;
    private ExecutionStage currentStage;
    private TaskConfiguration configuration;
    private String errorMessage;
    private LocalDateTime startTime;
    private LocalDateTime completedAt;
    private AuditInfo auditInfo;

    /**
     * 创建新任务
     */
    public static Task create(String taskName, TaskType taskType, Long createdBy) {
        if (taskName == null || taskName.trim().isEmpty()) {
            throw new IllegalArgumentException("Task name cannot be empty");
        }
        if (taskType == null) {
            throw new IllegalArgumentException("Task type cannot be null");
        }
        if (createdBy == null) {
            throw new IllegalArgumentException("Created by cannot be null");
        }

        Task task = new Task();
        task.id = null; // 数据库生成ID
        task.taskName = taskName;
        task.taskType = taskType;
        task.status = TaskStatus.PENDING;
        task.currentStage = ExecutionStage.CONTRACT_CLASSIFICATION;
        task.configuration = new TaskConfiguration();
        task.auditInfo = AuditInfo.create(createdBy);

        return task;
    }

    /**
     * 从现有数据重构任务实体
     */
    public static Task reconstruct(TaskId id, String taskName, TaskType taskType, TaskStatus status,
                                   ExecutionStage currentStage, TaskConfiguration configuration,
                                   String errorMessage, LocalDateTime startTime, LocalDateTime completedAt,
                                   AuditInfo auditInfo) {
        if (id == null || taskType == null || status == null || currentStage == null || auditInfo == null) {
            throw new IllegalArgumentException("Required fields cannot be null for reconstruction");
        }

        Task task = new Task();
        task.id = id;
        task.taskName = taskName;
        task.taskType = taskType;
        task.status = status;
        task.currentStage = currentStage;
        task.configuration = configuration != null ? configuration : new TaskConfiguration();
        task.errorMessage = errorMessage;
        task.startTime = startTime;
        task.completedAt = completedAt;
        task.auditInfo = auditInfo;

        return task;
    }

    /**
     * 初始化审计信息
     */
    public void initializeAuditInfo() {
        if (this.auditInfo == null) {
            this.auditInfo = AuditInfo.create(getCurrentUserId());
        }
    }

    /**
     * 取消任务
     */
    public void cancel() {
        validateStatusTransition(TaskStatus.CANCELLED);
        this.status = TaskStatus.CANCELLED;
        this.completedAt = LocalDateTime.now();
        updateAuditInfo();
    }

    /**
     * 启动任务
     */
    public void start() {
        validateStatusTransition(TaskStatus.RUNNING);
        this.status = TaskStatus.RUNNING;
        this.startTime = LocalDateTime.now();
        updateAuditInfo();
    }

    /**
     * 完成任务
     */
    public void complete() {
        validateStatusTransition(TaskStatus.COMPLETED);
        this.status = TaskStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
        updateAuditInfo();
    }

    /**
     * 任务失败
     */
    public void fail(String errorMessage) {
        if (errorMessage == null || errorMessage.trim().isEmpty()) {
            throw new IllegalArgumentException("Error message cannot be empty");
        }

        validateStatusTransition(TaskStatus.FAILED);
        this.status = TaskStatus.FAILED;
        this.completedAt = LocalDateTime.now();
        this.errorMessage = errorMessage;
        updateAuditInfo();
    }

    /**
     * 重试任务
     */
    public void retry() {
        if (!canRetry()) {
            throw new IllegalStateException("任务不能重试: 已达到最大重试次数");
        }

        this.status = TaskStatus.PENDING;
        this.startTime = null;
        this.completedAt = null;
        this.errorMessage = null;
        updateAuditInfo();
    }

    /**
     * 重置任务
     */
    public void reset() {
        this.status = TaskStatus.PENDING;
        updateAuditInfo();
    }

    /**
     * 检查是否可以重试
     */
    public boolean canRetry() {
        if (this.status != TaskStatus.FAILED) {
            return false;
        }

        if (this.configuration == null || this.configuration.getRetryPolicy() == null) {
            return true; // 如果没有配置，允许重试
        }

        return this.configuration.getRetryPolicy().getRetryCount() <
               this.configuration.getRetryPolicy().getMaxRetries();
    }

    /**
     * 检查是否超时
     */
    public boolean isTimeout() {
        if (this.startTime == null || this.status != TaskStatus.RUNNING) {
            return false;
        }

        if (this.configuration == null) {
            return false;
        }

        LocalDateTime timeoutTime = this.startTime.plusSeconds(this.configuration.getTimeoutSeconds());
        return LocalDateTime.now().isAfter(timeoutTime);
    }

    /**
     * 更新进度
     */
    public void updateProgress(int percentage) {
        if (percentage < 0 || percentage > 100) {
            throw new IllegalArgumentException("进度百分比必须在0-100之间");
        }

        updateAuditInfo();
    }

    /**
     * 状态转换
     */
    public void changeStatus(TaskStatus newStatus) {
        validateStatusTransition(newStatus);
        this.status = newStatus;

        if (newStatus == TaskStatus.CANCELLED) {
            this.completedAt = LocalDateTime.now();
        }
        updateAuditInfo();
    }

    /**
     * 验证状态转换
     */
    public void validateStatusTransition(TaskStatus targetStatus) {
        if (!isValidTransition(this.status, targetStatus)) {
            throw new IllegalStateException(
                String.format("无效的状态转换: %s -> %s", this.status, targetStatus)
            );
        }
    }

    /**
     * 检查状态转换是否有效
     */
    private boolean isValidTransition(TaskStatus from, TaskStatus to) {
        return switch (from) {
            case PENDING -> to == TaskStatus.RUNNING || to == TaskStatus.CANCELLED;
            case RUNNING -> to == TaskStatus.COMPLETED || to == TaskStatus.FAILED || to == TaskStatus.CANCELLED;
            case COMPLETED, CANCELLED -> false;
            case FAILED -> to == TaskStatus.PENDING; // 重试
        };
    }

    /**
     * 更新配置
     */
    public void updateConfiguration(TaskConfiguration configuration) {
        if (configuration == null) {
            throw new IllegalArgumentException("Configuration cannot be null");
        }

        this.configuration = configuration;
        updateAuditInfo();
    }

    /**
     * 更新当前执行阶段
     */
    public void updateCurrentStage(ExecutionStage stage) {
        if (stage == null) {
            throw new IllegalArgumentException("Stage cannot be null");
        }

        this.currentStage = stage;
        reset();
    }

    /**
     * 检查任务是否完成
     */
    public boolean isCompleted() {
        return this.status == TaskStatus.COMPLETED;
    }

    /**
     * 检查任务是否失败
     */
    public boolean isFailed() {
        return this.status == TaskStatus.FAILED;
    }

    /**
     * 检查任务是否取消
     */
    public boolean isCancelled() {
        return this.status == TaskStatus.CANCELLED;
    }

    /**
     * 检查任务是否运行中
     */
    public boolean isRunning() {
        return this.status == TaskStatus.RUNNING;
    }

    /**
     * 检查任务是否待处理
     */
    public boolean isPending() {
        return this.status == TaskStatus.PENDING;
    }

    /**
     * 执行任务 - 子类可以覆盖
     */
    public void execute() {
        // 默认实现，子类可以覆盖
        start();
        // 子类实现具体的任务逻辑
        complete();
    }

    /**
     * 更新审计信息
     */
    private void updateAuditInfo() {
        if (this.auditInfo != null) {
            this.auditInfo = this.auditInfo.update(getCurrentUserId());
        }
    }

    /**
     * 获取当前用户ID - 实际实现中应该从安全上下文获取
     */
    private Long getCurrentUserId() {
        // TODO: 从SecurityContext获取当前用户ID
        return 1L;
    }
}