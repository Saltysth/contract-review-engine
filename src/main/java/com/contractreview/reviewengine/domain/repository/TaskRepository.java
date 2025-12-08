package com.contractreview.reviewengine.domain.repository;

import com.contractreview.reviewengine.domain.enums.ExecutionStage;
import com.contractreview.reviewengine.domain.enums.TaskStatus;
import com.contractreview.reviewengine.domain.enums.TaskType;
import com.contractreview.reviewengine.domain.model.Task;
import com.contractreview.reviewengine.domain.model.TaskId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 任务仓储接口
 */
@Repository
public interface TaskRepository {

    /**
     * 保存任务
     */
    Task save(Task task);

    /**
     * 根据ID查找任务
     */
    Optional<Task> findById(TaskId taskId);

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
    List<Task> findByCreatedTimeBetween(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 查找超时的运行中任务
     */
    List<Task> findTimeoutTasks(TaskStatus status, LocalDateTime timeoutThreshold);

    /**
     * 分页查询任务
     */
    Page<Task> findByStatusOrderByCreatedTimeDesc(TaskStatus status, Pageable pageable);

    /**
     * 统计各状态任务数量
     */
    List<Object[]> countByStatus();

    /**
     * 检查任务是否存在
     */
    boolean existsById(TaskId taskId);

    /**
     * 根据ID删除任务
     */
    void deleteById(TaskId taskId);

    /**
     * 查找所有任务
     */
    List<Task> findAll();

    /**
     * 根据ID列表查找任务
     */
    List<Task> findAllById(Iterable<TaskId> taskIds);

    /**
     * 根据ID列表删除任务
     */
    void deleteAllById(Iterable<TaskId> taskIds);

    /**
     * 删除所有任务
     */
    void deleteAll();

    /**
     * 查找所有非最终状态的任务（按阶段处理）
     */
    List<Task> findNonFinalStageTasks();

    /**
     * 查找可重试的失败任务
     */
    List<Task> findRetryableTasks();

    /**
     * 根据执行阶段查找任务
     */
    List<Task> findByCurrentStage(ExecutionStage stage);

    /**
     * 根据状态和执行阶段查找任务
     */
    List<Task> findByStatusAndCurrentStage(TaskStatus status, ExecutionStage stage);

    boolean existByTaskNameAndNotThis(String taskName, Long id);
}