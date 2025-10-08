package com.contractreview.reviewengine.domain.repository;

import com.contractreview.reviewengine.domain.model.Task;
import com.contractreview.reviewengine.domain.model.TaskId;
import com.contractreview.reviewengine.domain.enums.TaskStatus;
import com.contractreview.reviewengine.domain.enums.TaskType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 任务仓储接口
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, TaskId> {
    
    /**
     * 根据状态查找任务
     */
    List<Task> findByStatus(TaskStatus status);
    
    /**
     * 根据任务类型查找任务
     */
    List<Task> findByTaskType(TaskType taskType);
    
    /**
     * 根据状态和任务类型查找任务
     */
    List<Task> findByStatusAndTaskType(TaskStatus status, TaskType taskType);
    
    /**
     * 查找指定时间范围内创建的任务
     */
    @Query("SELECT t FROM Task t WHERE t.auditInfo.createdAt BETWEEN :startTime AND :endTime")
    List<Task> findByCreatedAtBetween(@Param("startTime") LocalDateTime startTime, 
                                     @Param("endTime") LocalDateTime endTime);
    
    /**
     * 查找超时的运行中任务
     */
    @Query("SELECT t FROM Task t WHERE t.status = :status AND t.auditInfo.updatedAt < :timeoutThreshold")
    List<Task> findTimeoutTasks(@Param("status") TaskStatus status, 
                               @Param("timeoutThreshold") LocalDateTime timeoutThreshold);
    
    /**
     * 分页查询任务
     */
    Page<Task> findByStatusOrderByAuditInfoCreatedAtDesc(TaskStatus status, Pageable pageable);
    
    /**
     * 统计各状态任务数量
     */
    @Query("SELECT t.status, COUNT(t) FROM Task t GROUP BY t.status")
    List<Object[]> countByStatus();
    
    /**
     * 查找需要重试的失败任务
     */
    @Query("SELECT t FROM Task t WHERE t.status = :failedStatus AND t.retryCount < t.configuration.retryPolicy.maxRetries")
    List<Task> findRetryableTasks(@Param("failedStatus") TaskStatus failedStatus);
}