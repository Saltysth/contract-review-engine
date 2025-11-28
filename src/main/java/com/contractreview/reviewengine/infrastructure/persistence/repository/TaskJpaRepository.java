package com.contractreview.reviewengine.infrastructure.persistence.repository;

import com.contractreview.reviewengine.domain.enums.ExecutionStage;
import com.contractreview.reviewengine.domain.enums.TaskStatus;
import com.contractreview.reviewengine.domain.enums.TaskType;
import com.contractreview.reviewengine.infrastructure.persistence.entity.TaskEntity;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 任务JPA仓储接口
 *
 * @author SaltyFish
 */
@Repository
public interface TaskJpaRepository extends JpaRepository<TaskEntity, Long> {

    /**
     * 根据状态查找任务
     */
    List<TaskEntity> findByStatus(TaskStatus status);

    /**
     * 根据任务类型查找任务
     */
    List<TaskEntity> findByTaskType(TaskType taskType);

    /**
     * 根据状态和任务类型查找任务
     */
    List<TaskEntity> findByStatusAndTaskType(TaskStatus status, TaskType taskType);

    /**
     * 查找指定时间范围内创建的任务
     */
    @Query("SELECT t FROM TaskEntity t WHERE t.createdTime BETWEEN :startTime AND :endTime")
    List<TaskEntity> findByCreatedTimeBetween(@Param("startTime") LocalDateTime startTime,
                                             @Param("endTime") LocalDateTime endTime);

    /**
     * 查找超时的运行中任务
     */
    @Query("SELECT t FROM TaskEntity t WHERE t.status = :status AND t.updatedTime < :timeoutThreshold")
    List<TaskEntity> findTimeoutTasks(@Param("status") TaskStatus status,
                                     @Param("timeoutThreshold") LocalDateTime timeoutThreshold);

    /**
     * 分页查询任务
     */
    Page<TaskEntity> findByStatusOrderByCreatedTimeDesc(TaskStatus status, Pageable pageable);

    /**
     * 统计各状态任务数量
     */
    @Query("SELECT t.status, COUNT(t) FROM TaskEntity t GROUP BY t.status")
    List<Object[]> countByStatus();

    /**
     * 检查任务ID是否存在
     */
    boolean existsById(Long id);

    /**
     * 根据任务ID删除任务
     */
    void deleteById(Long id);

    /**
     * 查找所有非最终状态的任务（按阶段处理）
     * 只查找PENDING状态的任务，因为只有PENDING状态的任务才需要被处理
     */
    @Query("SELECT t FROM TaskEntity t WHERE t.currentStage != :finalStage AND (t.status = com.contractreview.reviewengine.domain.enums.TaskStatus.PENDING OR t.status = com.contractreview.reviewengine.domain.enums.TaskStatus.RUNNING) " +
           "ORDER BY t.createdTime ASC")
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<TaskEntity> findNonFinalStageTasks(@Param("finalStage") ExecutionStage finalStage);

    /**
     * 查找可重试的失败任务
     * 适配PostgreSQL 15的JSON操作+时间类型对比
     */
    @Query(value = "SELECT t.* FROM task t WHERE t.task_status = 'FAILED' " +
        "AND (t.configuration -> 'retryPolicy' ->> 'retryCount')::INTEGER < " +
        "COALESCE((t.configuration -> 'retryPolicy' ->> 'maxRetries')::INTEGER, 3) " +
        "AND (t.configuration -> 'retryPolicy' ->> 'nextRetryTime' IS NULL OR " +
        "     (t.configuration -> 'retryPolicy' ->> 'nextRetryTime')::TIMESTAMP <= :now) " +
        "ORDER BY t.updated_time ASC " +
        "FOR UPDATE",
        nativeQuery = true)
    List<TaskEntity> findRetryableTasks(@Param("now") LocalDateTime now);

    /**
     * 根据执行阶段查找任务
     */
    List<TaskEntity> findByCurrentStage(ExecutionStage currentStage);

    /**
     * 根据状态和执行阶段查找任务
     */
    List<TaskEntity> findByStatusAndCurrentStage(TaskStatus status, ExecutionStage currentStage);

}