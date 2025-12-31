package com.contractreview.reviewengine.infrastructure.persistence.repository;

import com.contractreview.reviewengine.domain.enums.ExecutionStage;
import com.contractreview.reviewengine.domain.enums.RiskLevel;
import com.contractreview.reviewengine.domain.enums.TaskStatus;
import com.contractreview.reviewengine.domain.repository.ContractTaskListRepository;
import com.contractreview.reviewengine.domain.valueobject.ReviewProgress;
import com.contractreview.reviewengine.interfaces.rest.dto.ContractTaskListItemDto;
import com.contractreview.reviewengine.interfaces.rest.dto.TaskListQueryRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private final ReviewResultJpaRepository reviewResultJpaRepository;

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

        // 计算总数（需要在边界检查前）
        long totalElements = countTotalTasks(queryRequest);

        // 手动分页
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), taskItems.size());

        // 边界检查：防止空列表或起始索引超出范围
        if (start >= taskItems.size()) {
            return new PageImpl<>(List.of(), pageable, totalElements);
        }

        List<ContractTaskListItemDto> pageContent = taskItems.subList(start, end);

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

    @Override
    @Cacheable(value = "riskDistribution", key = "'all'")
    public Map<String, Integer> getRiskDistribution() {
        log.debug("计算风险分布统计信息");
        List<Object[]> results = reviewResultJpaRepository.countByRiskLevel();
        Map<String, Integer> distribution = new HashMap<>();

        // 初始化所有风险等级为0
        Arrays.stream(RiskLevel.values())
            .forEach(level -> distribution.put(level.name(), 0));

        // 填充实际统计结果
        for (Object[] result : results) {
            String riskLevel = (String) result[0];
            Long count = (Long) result[1];
            if (riskLevel != null) {
                distribution.put(riskLevel, count.intValue());
            }
        }

        return distribution;
    }

    @Override
    @Cacheable(value = "riskTrend", key = "T(java.time.LocalDate).now().getYear() + '_' + T(java.time.LocalDate).now().getMonthValue()")
    public Integer calculateRiskTrend() {
        log.debug("计算风险趋势统计信息");
        // 定义高风险等级
        List<String> highRiskLevels = Arrays.asList(
            RiskLevel.HIGH.name(),
            RiskLevel.CRITICAL.name()
        );

        // 计算上月和本月的开始/结束时间
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime currentMonthStart = now.with(TemporalAdjusters.firstDayOfMonth()).withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime currentMonthEnd = now.with(TemporalAdjusters.lastDayOfMonth()).withHour(23).withMinute(59).withSecond(59).withNano(999999999);

        LocalDateTime lastMonthStart = currentMonthStart.minusMonths(1);
        LocalDateTime lastMonthEnd = currentMonthStart.minusSeconds(1);

        // 统计上月有风险的任务数
        Long lastMonthRiskCount = reviewResultJpaRepository.countRiskTasksInPeriod(
            lastMonthStart, lastMonthEnd, highRiskLevels);

        // 统计本月有风险的任务数
        Long currentMonthRiskCount = reviewResultJpaRepository.countRiskTasksInPeriod(
            currentMonthStart, currentMonthEnd, highRiskLevels);

        // 计算趋势百分比
        if (lastMonthRiskCount == 0) {
            return currentMonthRiskCount > 0 ? 100 : 0; // 如果上月为0，本月有风险则为100%增长，否则为0
        }

        double changePercent = ((double)(currentMonthRiskCount - lastMonthRiskCount) / lastMonthRiskCount) * 100;
        return (int) Math.round(changePercent);
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