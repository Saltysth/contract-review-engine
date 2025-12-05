package com.contractreview.reviewengine.domain.repository;

import com.contractreview.reviewengine.domain.enums.ReviewType;
import com.contractreview.reviewengine.domain.enums.TaskStatus;
import com.contractreview.reviewengine.interfaces.rest.dto.ContractTaskListItemDto;
import com.contractreview.reviewengine.interfaces.rest.dto.TaskListQueryRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

/**
 * 合同任务列表查询Repository接口
 *
 * @author SaltyFish
 */
public interface ContractTaskListRepository {

    /**
     * 分页查询任务列表
     *
     * @param queryRequest 查询参数
     * @param pageable 分页参数
     * @return 任务列表分页结果
     */
    Page<ContractTaskListItemDto> findTaskList(TaskListQueryRequestDto queryRequest, Pageable pageable);

    /**
     * 统计总任务数
     *
     * @param queryRequest 查询参数
     * @return 总任务数
     */
    long countTotalTasks(TaskListQueryRequestDto queryRequest);

    /**
     * 统计已完成任务数
     *
     * @return 已完成任务数（包括REVIEW_COMPLETED状态和终止态）
     */
    long countCompletedTasks();

    /**
     * 统计进行中任务数
     *
     * @return 进行中任务数
     */
    long countRunningTasks();

    /**
     * 计算平均执行时长（分钟）
     *
     * @return 平均执行时长
     */
    Integer calculateAverageDuration();
}