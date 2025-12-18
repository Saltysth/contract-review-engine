package com.contractreview.reviewengine.infrastructure.service;

import com.contractreview.exception.core.BusinessException;
import com.contractreview.exception.enums.CommonErrorCode;
import com.contractreview.reviewengine.domain.model.ContractReview;
import com.contractreview.reviewengine.domain.model.TaskId;
import com.contractreview.reviewengine.domain.repository.ContractReviewRepository;
import com.contractreview.reviewengine.domain.repository.ContractTaskListRepository;
import com.contractreview.reviewengine.interfaces.rest.dto.TaskListQueryRequestDto;
import com.contractreview.reviewengine.interfaces.rest.dto.TaskListResponseDto;
import com.contractreview.reviewengine.interfaces.rest.dto.TaskListStatisticsDto;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 合同任务基础设施服务
 * 专门处理一对一关系的合同任务查询和业务逻辑
 *
 * @author SaltyFish
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ContractTaskInfraService {

    private final ContractReviewRepository contractReviewRepository;
    private final ContractTaskListRepository contractTaskListRepository;

    /**
     * 根据任务ID查询对应的合同任务
     *
     * 由于task_id和contract_task是一一对应的关系，该方法会对结果进行严格校验
     * 如果task存在但对应的contract_task不存在，抛出业务异常
     *
     * @param taskId 任务ID
     * @return 对应的合同任务
     * @throws BusinessException 当task存在但contract_task不存在时抛出
     */
    @NotNull
    public ContractReview findContractTaskByTaskId(TaskId taskId) {
        if (taskId == null) {
            throw new BusinessException(CommonErrorCode.PARAM_INVALID, "任务ID不能为空");
        }

        log.debug("查询合同任务，taskId: {}", taskId.getValue());

        Optional<ContractReview> contractTaskOpt = contractReviewRepository.findByTaskId(taskId);

        if (contractTaskOpt.isEmpty()) {
            log.warn("未找到对应的合同任务，taskId: {}", taskId.getValue());
            throw new BusinessException(
                CommonErrorCode.DATA_NOT_FOUND,
                "任务ID [{0}] 对应的合同任务不存在，请确认任务类型和状态",
                taskId.getValue()
            );
        }

        ContractReview contractTask = contractTaskOpt.get();
        log.debug("成功找到合同任务，taskId: {}, contractId: {}", taskId.getValue(), contractTask.getContractId());

        return contractTask;
    }

    /**
     * 根据任务ID查询对应的合同任务（可选返回）
     *
     * 与findContractTaskByTaskId不同，此方法不会抛出异常，而是返回Optional
     * 用于需要区分"task不存在"和"contract_task不存在"的场景
     *
     * @param taskId 任务ID
     * @return 合同任务的Optional包装
     */
    public Optional<ContractReview> findContractTaskByTaskIdOptional(TaskId taskId) {
        if (taskId == null) {
            return Optional.empty();
        }

        log.debug("查询合同任务（可选返回），taskId: {}", taskId.getValue());

        return contractReviewRepository.findByTaskId(taskId);
    }

    /**
     * 验证任务ID是否有对应的合同任务
     *
     * @param taskId 任务ID
     * @return true如果存在对应的合同任务，false如果不存在
     */
    public boolean hasContractTask(TaskId taskId) {
        if (taskId == null) {
            return false;
        }

        return contractReviewRepository.findByTaskId(taskId).isPresent();
    }

    /**
     * 获取任务列表（带统计信息）
     */
    @Transactional(readOnly = true)
    public TaskListResponseDto getTaskListWithStatistics(TaskListQueryRequestDto queryRequest) {
        log.debug("查询任务列表，参数: {}", queryRequest);

        // 构建统计信息
        TaskListStatisticsDto statistics = buildStatistics();

        // 构建分页查询
        Pageable pageable = PageRequest.of(
            queryRequest.getPageNum(),
            queryRequest.getPageSize(),
            Sort.by(Sort.Direction.DESC, "createdTime")
        );

        // 查询任务列表
        var taskPage = contractTaskListRepository.findTaskList(queryRequest, pageable);

        return TaskListResponseDto.builder()
            .statistics(statistics)
            .tasks(taskPage.getContent())
            .total(taskPage.getTotalElements())
            .pageNum(queryRequest.getPageNum())
            .pageSize(queryRequest.getPageSize())
            .totalPages(taskPage.getTotalPages())
            .build();
    }

    /**
     * 构建统计信息
     */
    private TaskListStatisticsDto buildStatistics() {
        long totalTasks = contractTaskListRepository.countTotalTasks(new TaskListQueryRequestDto());
        long completedTasks = contractTaskListRepository.countCompletedTasks();
        long runningTasks = contractTaskListRepository.countRunningTasks();
        Integer averageDuration = contractTaskListRepository.calculateAverageDuration();

        return TaskListStatisticsDto.builder()
            .totalTasks((int) totalTasks)
            .completedTasks((int) completedTasks)
            .runningTasks((int) runningTasks)
            .averageDuration(averageDuration)
            .build();
    }
}