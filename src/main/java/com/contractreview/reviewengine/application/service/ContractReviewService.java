package com.contractreview.reviewengine.application.service;

import com.contract.common.constant.ContractStatus;
import com.contract.common.dto.DeleteClauseExtractionResponse;
import com.contract.common.feign.ClauseExtractionFeignClient;
import com.contract.common.feign.ContractFeignClient;
import com.contract.common.feign.dto.ContractFeignDTO;
import com.contractreview.reviewengine.domain.enums.ExecutionStage;
import com.contractreview.reviewengine.domain.enums.TaskType;
import com.contractreview.reviewengine.domain.model.ContractReview;
import com.contractreview.reviewengine.domain.model.ReviewResult;
import com.contractreview.reviewengine.domain.model.Task;
import com.contractreview.reviewengine.domain.model.TaskId;
import com.contractreview.reviewengine.domain.repository.ContractReviewRepository;
import com.contractreview.reviewengine.domain.repository.ReviewResultRepository;
import com.contractreview.reviewengine.domain.repository.TaskRepository;
import com.contractreview.reviewengine.domain.service.TaskManagementService;
import com.contractreview.reviewengine.domain.valueobject.TaskConfiguration;
import com.contractreview.reviewengine.infrastructure.service.ContractTaskInfraService;
import com.contractreview.reviewengine.interfaces.rest.converter.ContractReviewConverter;
import com.contractreview.reviewengine.interfaces.rest.dto.ContractReviewCreateRequestDto;
import com.contractreview.reviewengine.interfaces.rest.dto.ContractReviewRequestDto;
import com.contractreview.reviewengine.interfaces.rest.dto.TaskListQueryRequestDto;
import com.contractreview.reviewengine.interfaces.rest.dto.TaskListResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        ReviewResult savedResult = reviewResultRepository.save(reviewResult);
        log.info("Saved review result for task: {}", reviewResult.getTaskId());

        // 更新任务状态为完成
        taskManagementService.completeTask(TaskId.of(reviewResult.getTaskId()));

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
            taskManagementService.updateTaskConfiguration(taskId, newTaskConfig);
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
        if (!ContractStatus.DRAFT.equals(requestDto.getContractStatus())) {
            log.warn("Contract task cannot be updated: {} (status is not draft)", requestDto.getContractId());
            throw new IllegalArgumentException("Contract task cannot be updated: status is not draft");
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
}