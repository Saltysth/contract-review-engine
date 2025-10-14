package com.contractreview.reviewengine.domain.model;

import com.contractreview.reviewengine.domain.enums.ExecutionStage;
import com.contractreview.reviewengine.domain.enums.TaskStatus;
import com.contractreview.reviewengine.domain.enums.TaskType;
import com.contractreview.reviewengine.domain.valueobject.AuditInfo;
import com.contractreview.reviewengine.domain.valueobject.TaskConfiguration;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

/**
 * 任务聚合根 - 基类
 * 
 * @author SaltyFish
 */
@Entity
@Table(name = "task")
@Inheritance(strategy = InheritanceType.JOINED)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Task {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "task_name", nullable = false)
    private String taskName;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "task_type", nullable = false)
    private TaskType taskType;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "task_status", nullable = false)
    @Builder.Default
    private TaskStatus status = TaskStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_stage", nullable = false)
    @Builder.Default
    private ExecutionStage currentStage = ExecutionStage.CONTRACT_CLASSIFICATION;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "configuration", columnDefinition = "jsonb")
    @Builder.Default
    private TaskConfiguration configuration = new TaskConfiguration();

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "start_time")
    private LocalDateTime startTime;
    
    @Column(name = "end_time")
    private LocalDateTime completedAt;
    
    @Embedded
    private AuditInfo auditInfo;
    
    protected Task(String taskName, TaskType taskType, Long createdBy) {
        this.taskName = taskName;
        this.taskType = taskType;
        this.status = TaskStatus.PENDING;
        this.auditInfo = AuditInfo.create(createdBy);
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
        this.status = TaskStatus.FAILED;
        this.completedAt = LocalDateTime.now();
        updateAuditInfo();
    }
    
    /**
     * 重试任务 TODO move
     */
    public void retry() {
        if (!canRetry()) {
            throw new IllegalStateException("任务不能重试: 已达到最大重试次数");
        }
        
//        this.retryCount++;
        this.status = TaskStatus.PENDING;
        this.startTime = null;
        this.completedAt = null;
        updateAuditInfo();
    }
    
    /**
     * 检查是否可以重试 TODO move
     */
    public boolean canRetry() {
        return this.status == TaskStatus.FAILED;
    }
    
    /**
     * 检查是否超时 TODO move
     */
    public boolean isTimeout() {
        if (this.startTime == null || this.status != TaskStatus.RUNNING) {
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
    
    /**
     * 执行任务 - 子类可以覆盖
     */
    public void execute() {
        // 默认实现，子类可以覆盖
    }
}