package com.contractreview.reviewengine.application.service;

import com.contract.common.dto.DeleteClauseExtractionResponse;
import com.contract.common.feign.ClauseExtractionFeignClient;
import com.contract.common.feign.ClauseFeignClient;
import com.contract.common.feign.ContractFeignClient;
import com.contract.common.feign.dto.ContractFeignDTO;
import com.contractreview.reviewengine.domain.enums.TaskType;
import com.contractreview.reviewengine.domain.model.ContractReview;
import com.contractreview.reviewengine.domain.model.ReviewResult;
import com.contractreview.reviewengine.domain.model.Task;
import com.contractreview.reviewengine.domain.model.TaskId;
import com.contractreview.reviewengine.domain.repository.ContractReviewRepository;
import com.contractreview.reviewengine.domain.repository.ReviewResultRepository;
import com.contractreview.reviewengine.domain.service.TaskManagementService;
import com.contractreview.reviewengine.domain.valueobject.ReviewProgress;
import com.contractreview.reviewengine.domain.valueobject.TaskConfiguration;
import com.contractreview.reviewengine.infrastructure.persistence.repository.TaskEntityRepository;
import com.contractreview.reviewengine.infrastructure.service.ContractTaskInfraService;
import com.contractreview.reviewengine.interfaces.rest.converter.ContractReviewConverter;
import com.contractreview.reviewengine.interfaces.rest.dto.ContractReviewCreateRequestDto;
import com.contractreview.reviewengine.interfaces.rest.dto.ContractReviewRequestDto;
import com.contractreview.reviewengine.interfaces.rest.dto.ContractTaskDetailDto;
import com.contractreview.reviewengine.interfaces.rest.dto.TaskListQueryRequestDto;
import com.contractreview.reviewengine.interfaces.rest.dto.TaskListResponseDto;
import com.contractreview.reviewengine.interfaces.rest.dto.TaskProgressDto;
import com.contractreview.reviewengine.domain.enums.ExecutionStage;
import com.contractreview.reviewengine.domain.enums.TaskStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

/**
 * 合同审查应用服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ContractReviewService {
    private static final int ONE = 1;
    private final ContractFeignClient contractFeignClient;

    private final TaskManagementService taskManagementService;
    private final ContractReviewRepository contractReviewRepository;
    private final ReviewResultRepository reviewResultRepository;
    private final ContractReviewConverter contractReviewConverter;
    private final TaskService taskService;
    private final ContractTaskInfraService contractTaskInfraService;
    private final ClauseExtractionFeignClient clauseExtractionFeignClient;
    private final TaskEntityRepository taskEntityRepository;
    private final ClauseFeignClient clauseFeignClient;

    /**
     * 创建合同审查任务
     */
    public ContractReview createContractReviewTask(ContractReviewCreateRequestDto requestDto) {
        ContractFeignDTO contract = contractFeignClient.createContract(requestDto.getFileUuid());
        Long contractId = contract.getId();

        TaskConfiguration config = TaskConfiguration.defaultTaskConfiguration();
        // 创建基础任务，初始状态设为条款抽取阶段
        Task task = taskManagementService.createTask(TaskType.CLASSIFICATION, config);

        // 创建合同审查
        ContractReview contractReview = ContractReview.create(
                task.getId().getValue(),
                contractId,
                contract.getAttachmentUuid()
        );

        ContractReview savedReview = contractReviewRepository.save(contractReview);
        log.info("Created contract review task: {} for contract: {}, initial stage: CLAUSE_EXTRACTION",
                task.getId(), contractId);

        return savedReview;
    }

    /**
     * 获取合同任务
     */
    @Transactional(readOnly = true)
    public ContractReview getContractTask(TaskId taskId) {
        return contractReviewRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Contract review not found: " + taskId));
    }

    /**
     * 根据任务ID严格查询对应的合同任务
     *
     * 该方法验证taskId与contract_task之间的一一对应关系。
     * 如果task存在但对应的contract_task不存在，将抛出BusinessException。
     *
     * @param taskId 任务ID
     * @return 对应的合同任务
     * @throws com.contractreview.exception.core.BusinessException 当task存在但contract_task不存在时抛出
     */
    @Transactional(readOnly = true)
    public ContractReview getContractTaskByTaskId(TaskId taskId) {
        log.info("严格查询合同任务，taskId: {}", taskId.getValue());

        ContractReview contractTask = contractTaskInfraService.findContractTaskByTaskId(taskId);

        log.info("成功查询到合同任务，taskId: {}, contractId: {}", taskId.getValue(), contractTask.getContractId());
        return contractTask;
    }
    
    /**
     * 根据合同ID获取任务列表
     */
    @Transactional(readOnly = true)
    public List<ContractReview> getTasksByContractId(Long contractId) {
        return contractReviewRepository.findByContractId(contractId);
    }
    
    /**
     * 获取合同的最新任务
     */
    @Transactional(readOnly = true)
    public Optional<ContractReview> getLatestTaskByContractId(Long contractId) {
        List<ContractReview> reviews = contractReviewRepository.findLatestByContractId(contractId);
        return reviews.isEmpty() ? Optional.empty() : Optional.of(reviews.get(0));
    }
    
    /**
     * 保存审查结果
     */
    public ReviewResult saveReviewResult(ReviewResult reviewResult) {
        // 建立双向关联关系：设置每个规则结果的父引用
        if (reviewResult.getRuleResults() != null) {
            reviewResult.getRuleResults().forEach(ruleResult -> 
                ruleResult.setReviewResult(reviewResult)
            );
        }
        
        ReviewResult savedResult = reviewResultRepository.save(reviewResult);
        log.info("Saved review result for task: {}", reviewResult.getTaskId());

        return savedResult;
    }
    
    /**
     * 获取审查结果
     */
    @Transactional(readOnly = true)
    public Optional<ReviewResult> getReviewResult(TaskId taskId) {
        return reviewResultRepository.findByTaskId(taskId);
    }

    /**
     * 处理审查失败
     */
    public void handleReviewFailure(TaskId taskId, String errorMessage) {
        taskManagementService.failTask(taskId, errorMessage);

        // TODO: 发送失败通知
        log.error("Contract review failed for task: {} - {}", taskId, errorMessage);
    }

    /**
     * 启动合同审查任务
     * 任务将由定时任务调度器自动处理，此方法仅用于触发任务状态更新
     *
     * @param taskId 任务ID
     */
    public void startContractReview(TaskId taskId) {
        log.info("启动合同审查任务，将由状态聚合执行器自动处理: {}", taskId);

        // 获取任务并验证状态
        Task task = taskService.getTaskById(taskId);

        // 验证任务是否处于待处理状态
        if (!task.isPending()) {
            log.warn("任务 {} 状态不是待处理状态，当前状态: {}", taskId, task.getStatus());
            throw new IllegalStateException("任务状态不正确，无法启动审查");
        }

        // 任务将在定时任务中被处理，这里只需要记录日志
        log.info("任务 {} 已准备就绪，等待状态聚合执行器处理", taskId);
    }

    public Boolean deleteContractReviewTask(Long contractTaskId) {
        // 查询主数据的task
        Optional<ContractReview> contractReview = contractReviewRepository.findById(TaskId.of(contractTaskId));

        // 如果不存在，直接返回false
        if (contractReview.isEmpty()) {
            return false;
        }

        Boolean response =
            contractFeignClient.deleteContract(contractReview.get().getContractId());
        if (Boolean.FALSE.equals(response)) {
            log.warn("合同删除失败，未删除合同任务");
            return false;
        }
        log.info("合同删除成功");

        // 如果存在，执行删除并返回删除结果
        ContractReview mainTask = contractReview.get();
        return taskManagementService.deleteTask(TaskId.of(mainTask.getTaskId()));
    }

    public ContractReview updateContractReviewTask(@Valid ContractReviewRequestDto requestDto) {
        validateContractTaskStatusForUpdate(requestDto);

        // 获取要更新的合同审查任务
        Optional<ContractReview> existingReviewOpt = getLatestTaskByContractId(requestDto.getContractId());
        if (existingReviewOpt.isEmpty()) {
            throw new IllegalArgumentException("No contract review task found for contract: " + requestDto.getContractId());
        }

        ContractReview existingReview = existingReviewOpt.get();
        TaskId taskId = TaskId.of(existingReview.getTaskId());

        // 根据DTO中的不同部分，检查是否需要更新并调用相应的领域方法

        // 1. 检查并更新任务配置（task部分）
        var currentTaskConfig = taskManagementService.getTaskConfiguration(taskId);
        if (contractReviewConverter.needsTaskConfigurationUpdate(requestDto, currentTaskConfig)) {
            TaskConfiguration newTaskConfig = contractReviewConverter.convertToTaskConfiguration(requestDto);
            taskManagementService.updateTaskConfiguration(taskId, newTaskConfig, requestDto.getContractTitle());
            log.info("Updated task configuration for task: {}", taskId);
        }

        // 2. 检查并更新合同元数据（contract部分 - 这里只有业务标签可更新）
        if (contractReviewConverter.needsContractMetadataUpdate(requestDto, existingReview.getContractMetadata())) {
            var newContractMetadata = contractReviewConverter.convertToContractMetadata(requestDto);
            existingReview.updateContractMetadata(newContractMetadata);
            log.info("Updated contract metadata for task: {}", taskId);
        }

        // 3. 检查并更新审查配置（contract_task部分）
        if (contractReviewConverter.needsReviewConfigurationUpdate(requestDto, existingReview.getReviewConfiguration())) {
            var newReviewConfig = contractReviewConverter.convertToReviewConfiguration(requestDto);
            existingReview.updateReviewConfiguration(newReviewConfig);
            log.info("Updated review configuration for task: {}", taskId);
        }

        // 保存更新后的合同审查
        ContractReview savedReview = contractReviewRepository.save(existingReview);
        log.info("Successfully updated contract review task: {} for contract: {}", taskId, requestDto.getContractId());

        return savedReview;
    }

    private void validateContractTaskStatusForUpdate(@Valid ContractReviewRequestDto requestDto) {
        if (requestDto.getContractId() == null) {
            log.warn("contractId cannot be null");
            throw new IllegalArgumentException("contractId cannot be null");
        }
    }

    /**
     * 重试任务
     */
    @Transactional
    public void retryTask(TaskId taskId) {
        Task task = taskService.getTaskById(taskId);
        // 不仅要重制任务状态，还需要把相关步骤的生成结果全部软删除。
        if (task.canRetry()) {
            taskService.retryTask(task);
            // FUTURE 以后可能不止一种类型
            ContractReview contractReview = contractTaskInfraService.findContractTaskByTaskId(taskId);
            DeleteClauseExtractionResponse deleteClauseExtractionResponse =
                clauseExtractionFeignClient.deleteClauseExtraction(contractReview.getContractId());

            if (!deleteClauseExtractionResponse.getSuccess()) {
                log.info("重置任务状态失败， contractId:{}", contractReview.getContractId());
            } else {
                log.info("重置任务状态成功， contractId:{}", contractReview.getContractId());
            }

            int retryCount = task.getConfiguration() != null && task.getConfiguration().getRetryPolicy() != null
                ? task.getConfiguration().getRetryPolicy().getRetryCount()
                : 0;
            log.info("Retrying task: {} (attempt {})", taskId, retryCount);
        } else {
            log.warn("Task cannot be retried: {} (max retries exceeded)", taskId);
            throw new IllegalStateException("Task cannot be retried: max retries exceeded");
        }
    }

    /**
     * 获取任务列表（带统计信息）
     */
    @Transactional(readOnly = true)
    public TaskListResponseDto getTaskList(TaskListQueryRequestDto queryRequest) {
        return contractTaskInfraService.getTaskListWithStatistics(queryRequest);
    }

    public ContractTaskDetailDto getContractTaskDetailByTaskId(TaskId taskId) {
        log.info("查询合同任务详情，taskId: {}", taskId.getValue());

        // 使用JPQL查询直接获取ContractTaskDetailDto
        ContractTaskDetailDto taskDetail = taskEntityRepository.findTaskDetailByTaskId(taskId.getValue());

        if (taskDetail == null) {
            log.warn("未找到合同任务详情，taskId: {}", taskId.getValue());
            throw new IllegalArgumentException("Contract task not found for taskId: " + taskId.getValue());
        }

        // 丰富进度信息
        if (taskDetail.getCurrentStage() != null) {
            ReviewProgress progress = new ReviewProgress(taskDetail.getCurrentStage());
            taskDetail.setProgressDetail(progress);
            taskDetail.setProgressStr(progress.getProgress() + "%");
        }

        log.info("成功查询到合同任务详情，taskId: {}, contractId: {}",
            taskId.getValue(), taskDetail.getContractId());

        return taskDetail;
    }

    /**
     * 获取任务实时进度
     */
    @Transactional(readOnly = true)
    public TaskProgressDto getTaskProgress(TaskId taskId) {
        log.info("获取任务进度，taskId: {}", taskId.getValue());

        // 获取任务信息
        Task task = taskService.getTaskById(taskId);
        if (task == null) {
            throw new IllegalArgumentException("Task not found: " + taskId.getValue());
        }

        // 获取合同审查信息以获取合同ID
        ContractReview contractReview = contractTaskInfraService.findContractTaskByTaskId(taskId);
        if (contractReview == null) {
            throw new IllegalArgumentException("Contract review not found for task: " + taskId.getValue());
        }

        // 获取合同条款数量 - 暂时使用模拟数据，实际应该调用clauseExtractionFeignClient获取
        int totalClauses = clauseFeignClient.getClausesByContractId(contractReview.getContractId()).size();

        // 计算进度
        TaskProgressDto.TaskStatisticsDto statistics = calculateTaskStatistics(task, totalClauses);

        // 计算进度百分比
        double progress = calculateProgress(task, statistics);

        // 计算预计剩余时间
        String estimatedTimeRemaining = calculateEstimatedTimeRemaining(task, statistics, totalClauses);

        // 计算平均每个条款耗时
        Long averageItemDuration = calculateAverageItemDuration(task, statistics);

        return TaskProgressDto.builder()
                .taskId(taskId.getValue().toString())
                .currentStage(mapCurrentStage(task.getCurrentStage()))
                .progress(progress)
                .statistics(statistics)
                .estimatedTimeRemaining(estimatedTimeRemaining)
                .startTime(task.getStartTime())
                .averageItemDuration(averageItemDuration)
                .build();
    }

    /**
     * 计算任务统计信息
     */
    private TaskProgressDto.TaskStatisticsDto calculateTaskStatistics(Task task, int totalClauses) {
        TaskStatus status = task.getStatus();

        // 由于粒度是合同级别而不支持条款级别，所以根据合同任务的状态统一设置
        if (status == TaskStatus.COMPLETED) {
            return TaskProgressDto.TaskStatisticsDto.builder()
                    .total(totalClauses)
                    .completed(totalClauses)
                    .running(0)
                    .failed(0)
                    .skipped(0) // TODO: 实现跳过功能
                    .build();
        } else if (status == TaskStatus.FAILED) {
            return TaskProgressDto.TaskStatisticsDto.builder()
                    .total(totalClauses)
                    .completed(0)
                    .running(0)
                    .failed(totalClauses)
                    .skipped(0)
                    .build();
        } else if (status == TaskStatus.RUNNING) {
            // 根据当前阶段计算进度
            ExecutionStage currentStage = task.getCurrentStage();
            ExecutionStage[] stages = ExecutionStage.values();
            int currentStageIndex = currentStage.ordinal();
            int totalStages = stages.length - 1; // 不包括REVIEW_COMPLETED

            // 计算已完成的阶段数
            int completedStages = currentStageIndex;
            // 计算当前阶段的进度
            double stageProgress = getStageProgress(currentStage, task);

            int completedClauses = (int) ((completedStages + stageProgress) * totalClauses / totalStages);
            int runningClauses = totalClauses - completedClauses;

            return TaskProgressDto.TaskStatisticsDto.builder()
                    .total(totalClauses)
                    .completed(completedClauses)
                    .running(runningClauses)
                    .failed(0)
                    .skipped(0)
                    .build();
        } else {
            // PENDING 或 CANCELLED
            return TaskProgressDto.TaskStatisticsDto.builder()
                    .total(totalClauses)
                    .completed(0)
                    .running(0)
                    .failed(0)
                    .skipped(0)
                    .build();
        }
    }

    /**
     * 获取当前阶段的进度（简化实现）
     */
    private double getStageProgress(ExecutionStage stage, Task task) {
        // 简化实现，实际应该根据具体业务逻辑计算
        if (task.getStatus() == TaskStatus.RUNNING) {
            // 假设每个阶段进度为50%（正在执行中）
            return 0.5;
        }
        return 0.0;
    }

    /**
     * 计算总体进度百分比
     */
    private double calculateProgress(Task task, TaskProgressDto.TaskStatisticsDto statistics) {
        if (statistics.getTotal() == 0) {
            return 0.0;
        }

        TaskStatus status = task.getStatus();
        if (status == TaskStatus.COMPLETED) {
            return 100.0;
        } else if (status == TaskStatus.FAILED) {
            return 0.0;
        } else {
            // 基于已完成的条款数量计算进度
            return (double) statistics.getCompleted() / statistics.getTotal() * 100;
        }
    }

    /**
     * 计算预计剩余时间
     */
    private String calculateEstimatedTimeRemaining(Task task, TaskProgressDto.TaskStatisticsDto statistics, int totalClauses) {
        TaskStatus status = task.getStatus();

        if (status == TaskStatus.COMPLETED || status == TaskStatus.CANCELLED) {
            return "0分钟";
        }

        if (status == TaskStatus.PENDING) {
            return "未开始";
        }

        // 计算时间系数
        double timeCoefficient = calculateTimeCoefficient(task);

        // 计算剩余条款数量
        int remainingClauses = statistics.getRunning() + statistics.getFailed() + statistics.getSkipped();

        // 计算预计时间（每个条款10秒基准）
        double estimatedSeconds = remainingClauses * 10 * timeCoefficient;

        // 如果任务已开始，减去已执行时间
        if (task.getStartTime() != null && status == TaskStatus.RUNNING) {
            long elapsedSeconds = Duration.between(task.getStartTime(), java.time.LocalDateTime.now()).getSeconds();
            double remainingSeconds = Math.max(0, estimatedSeconds - elapsedSeconds);
            estimatedSeconds = remainingSeconds;
        }

        // 转换为分钟
        int minutes = (int) Math.ceil(estimatedSeconds / 60);
        return minutes + "分钟";
    }

    /**
     * 计算时间系数
     */
    private double calculateTimeCoefficient(Task task) {
        if (task.getConfiguration() == null) {
            return 1.0; // 默认系数
        }

        // 根据审查类型和提示词类型计算系数
        // TODO: 根据实际的配置字段实现
        return 1.0;
    }

    /**
     * 计算平均每个条款耗时
     */
    private Long calculateAverageItemDuration(Task task, TaskProgressDto.TaskStatisticsDto statistics) {
        if (task.getStartTime() == null || task.getCompletedAt() == null) {
            return null; // 任务未完成，无法计算平均耗时
        }

        if (statistics.getCompleted() == 0) {
            return null;
        }

        long totalDurationMillis = Duration.between(task.getStartTime(), task.getCompletedAt()).toMillis();
        return totalDurationMillis / statistics.getCompleted();
    }

    /**
     * 映射当前阶段到显示名称
     */
    private String mapCurrentStage(ExecutionStage stage) {
        if (stage == null) {
            return "未开始";
        }

        return switch (stage) {
            case CONTRACT_CLASSIFICATION -> "合同分类";
            case CLAUSE_EXTRACTION -> "条款抽取";
            case KNOWLEDGE_MATCHING -> "知识库匹配";
            case MODEL_REVIEW -> "模型审查";
            case RESULT_VALIDATION -> "结果校验";
            case REPORT_GENERATION -> "报告生成";
            case REVIEW_COMPLETED -> "审查完毕";
        };
    }
}