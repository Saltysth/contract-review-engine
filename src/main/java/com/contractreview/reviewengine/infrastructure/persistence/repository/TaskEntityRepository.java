package com.contractreview.reviewengine.infrastructure.persistence.repository;

import com.contractreview.reviewengine.domain.enums.ExecutionStage;
import com.contractreview.reviewengine.domain.enums.TaskStatus;
import com.contractreview.reviewengine.infrastructure.persistence.entity.TaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Task实体JPA Repository
 *
 * @author SaltyFish
 */
@Repository
public interface TaskEntityRepository extends JpaRepository<TaskEntity, Long> {

    /**
     * 查找已完成任务（包括REVIEW_COMPLETED状态和终止态），用于统计
     */
    List<TaskEntity> findByCurrentStageOrStatusIn(ExecutionStage reviewCompleted, List<TaskStatus> statusList);

    /**
     * 查找已完成任务（包括REVIEW_COMPLETED状态和终止态），且有开始和结束时间的，用于计算平均时长
     */
    List<TaskEntity> findByCurrentStageOrStatusInAndStartTimeIsNotNullAndEndTimeIsNotNull(
        ExecutionStage reviewCompleted, List<TaskStatus> statusList);

    /**
     * 统计已完成任务数量（包括REVIEW_COMPLETED状态和终止态）
     */
    long countByCurrentStageOrStatusIn(ExecutionStage reviewCompleted, List<TaskStatus> statusList);

    /**
     * 统计进行中任务数量
     */
    long countByStatus(TaskStatus status);

    /**
     * 使用原生SQL查询平均执行时长
     *
     * @param reviewCompleted 审查完成阶段
     * @param statusList 状态列表
     * @return 平均执行时长（分钟）
     */
    @Query(value = """
    SELECT COALESCE(AVG(EXTRACT(EPOCH FROM (end_time - created_time)) / 60), 0)
    FROM task
    WHERE (current_stage = :reviewCompleted OR task_status IN (:statusList))
    AND start_time IS NOT NULL
    AND created_time IS NOT NULL
    """, nativeQuery = true)
    Double calculateAverageDurationInMinutes(
        @Param("reviewCompleted") String reviewCompleted,
        @Param("statusList") List<String> statusList);

    /**
     * 统计总任务数（带过滤条件）
     */
    @Query("""
        SELECT COUNT(t)
        FROM TaskEntity t, ContractTaskEntity ct
        WHERE ct.taskId = t.id
        AND (:taskName IS NULL OR t.taskName LIKE %:taskName%)
        AND (:contractType IS NULL OR ct.reviewType = :contractType)
        AND (:taskStatus IS NULL OR t.status = :taskStatus)
        """)
    long countWithFilters(
        @Param("taskName") String taskName,
        @Param("contractType") com.contractreview.reviewengine.domain.enums.ReviewType contractType,
        @Param("taskStatus") TaskStatus taskStatus);

    /**
     * 分页查询任务列表（带过滤条件）
     */
    @Query("""
        SELECT new com.contractreview.reviewengine.interfaces.rest.dto.ContractTaskListItemDto(
            CAST(t.id AS string),
            ct.contractId,
            ct.fileUuid,
            t.configuration,
            t.taskName,
            ct.reviewType,
            t.status,
            t.currentStage,
            t.errorMessage,
            t.startTime,
            t.endTime,
            t.createdTime
        )
        FROM TaskEntity t, ContractTaskEntity ct
        WHERE ct.taskId = t.id
        AND (:taskName IS NULL OR t.taskName LIKE %:taskName%)
        AND (:contractType IS NULL OR ct.reviewType = :contractType)
        AND (:taskStatus IS NULL OR t.status = :taskStatus)
        ORDER BY t.createdTime DESC
        """)
    List<com.contractreview.reviewengine.interfaces.rest.dto.ContractTaskListItemDto> findTaskListWithFilters(
        @Param("taskName") String taskName,
        @Param("contractType") com.contractreview.reviewengine.domain.enums.ReviewType contractType,
        @Param("taskStatus") TaskStatus taskStatus);
}