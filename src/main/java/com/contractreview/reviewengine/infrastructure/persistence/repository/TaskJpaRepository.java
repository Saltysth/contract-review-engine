package com.contractreview.reviewengine.infrastructure.persistence.repository;

import com.contractreview.reviewengine.domain.enums.TaskStatus;
import com.contractreview.reviewengine.domain.enums.TaskType;
import com.contractreview.reviewengine.infrastructure.persistence.entity.TaskEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
     * 根据任务ID查找任务实体
     */
    TaskEntity findByTaskId(Long taskId);

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
    boolean existsByTaskId(Long taskId);

    /**
     * 根据任务ID删除任务
     */
    void deleteByTaskId(Long taskId);
}