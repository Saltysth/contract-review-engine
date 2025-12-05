package com.contractreview.reviewengine.infrastructure.persistence.repository;

import com.contractreview.reviewengine.domain.enums.ExecutionStage;
import com.contractreview.reviewengine.domain.enums.TaskStatus;
import com.contractreview.reviewengine.domain.repository.ContractTaskListRepository;
import com.contractreview.reviewengine.interfaces.rest.dto.ContractTaskListItemDto;
import com.contractreview.reviewengine.interfaces.rest.dto.TaskListQueryRequestDto;
import com.contractreview.reviewengine.domain.valueobject.ReviewProgress;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 合同任务列表查询Repository实现
 *
 * @author SaltyFish
 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class ContractTaskListRepositoryImpl implements ContractTaskListRepository {

    private final TaskEntityRepository taskEntityRepository;

    @Override
    public Page<ContractTaskListItemDto> findTaskList(TaskListQueryRequestDto queryRequest, Pageable pageable) {
        log.debug("查询任务列表，参数: {}", queryRequest);

        // 使用@Query查询
        List<ContractTaskListItemDto> taskItems = taskEntityRepository.findTaskListWithFilters(
            queryRequest.getTaskName(),
            queryRequest.getContractType(),
            queryRequest.getTaskStatus()
        );

        // 设置进度信息
        taskItems = taskItems.stream()
            .map(this::enrichProgressInfo)
            .collect(Collectors.toList());

        // 手动分页
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), taskItems.size());
        List<ContractTaskListItemDto> pageContent = taskItems.subList(start, end);

        long totalElements = countTotalTasks(queryRequest);

        return new PageImpl<>(pageContent, pageable, totalElements);
    }

    @Override
    public long countTotalTasks(TaskListQueryRequestDto queryRequest) {
        return taskEntityRepository.countWithFilters(
            queryRequest.getTaskName(),
            queryRequest.getContractType(),
            queryRequest.getTaskStatus()
        );
    }

    @Override
    public long countCompletedTasks() {
        List<TaskStatus> completedStatuses = List.of(
            TaskStatus.COMPLETED,
            TaskStatus.FAILED,
            TaskStatus.CANCELLED
        );
        return taskEntityRepository.countByCurrentStageOrStatusIn(ExecutionStage.REVIEW_COMPLETED, completedStatuses);
    }

    @Override
    public long countRunningTasks() {
        return taskEntityRepository.countByStatus(TaskStatus.RUNNING);
    }

    @Override
    public Integer calculateAverageDuration() {
        List<TaskStatus> completedStatuses = List.of(
            TaskStatus.COMPLETED,
            TaskStatus.FAILED,
            TaskStatus.CANCELLED
        );

        // 使用原生SQL查询计算平均时长
        List<String> statusStrings = completedStatuses.stream()
            .map(TaskStatus::name)
            .collect(Collectors.toList());

        Double avgMinutes = taskEntityRepository.calculateAverageDurationInMinutes(
            ExecutionStage.REVIEW_COMPLETED.name(),
            statusStrings
        );

        return avgMinutes != null ? avgMinutes.intValue() : 0;
    }

    /**
     * 丰富进度信息
     */
    private ContractTaskListItemDto enrichProgressInfo(ContractTaskListItemDto item) {
        ExecutionStage stage = item.getCurrentStage();
        if (stage != null) {
            ReviewProgress progress = new ReviewProgress(stage);
            item.setProgressDetail(progress);
            item.setProgress(progress.getProgress() + "%");
        }
        return item;
    }
}