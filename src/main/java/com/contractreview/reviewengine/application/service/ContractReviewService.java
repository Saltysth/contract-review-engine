package com.contractreview.reviewengine.application.service;

import com.contract.common.constant.ContractStatus;
import com.contract.common.feign.ContractFeignClient;
import com.contract.common.feign.dto.ContractFeignDTO;
import com.contractreview.reviewengine.domain.enums.TaskType;
import com.contractreview.reviewengine.domain.model.ContractReview;
import com.contractreview.reviewengine.domain.model.ReviewResult;
import com.contractreview.reviewengine.domain.model.TaskId;
import com.contractreview.reviewengine.domain.repository.ContractReviewRepository;
import com.contractreview.reviewengine.domain.repository.ReviewResultRepository;
import com.contractreview.reviewengine.domain.service.TaskManagementService;
import com.contractreview.reviewengine.domain.valueobject.TaskConfiguration;
import com.contractreview.reviewengine.interfaces.rest.converter.ContractReviewConverter;
import com.contractreview.reviewengine.interfaces.rest.dto.ContractReviewCreateRequestDto;
import com.contractreview.reviewengine.interfaces.rest.dto.ContractReviewRequestDto;
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
    
    /**
     * 创建合同审查任务
     */
    public ContractReview createContractReviewTask(ContractReviewCreateRequestDto requestDto) {
        ContractFeignDTO contract = contractFeignClient.createContract(requestDto.getFileUuid());
        Long contractId = contract.getId();

        // 创建基础任务
        var task = taskManagementService.createTask(TaskType.CLASSIFICATION, TaskConfiguration.defaultTaskConfiguration());

        // 创建合同审查
        ContractReview contractReview = ContractReview.create(
                task.getId().getValue(),
                contractId,
                contract.getAttachmentUuid()
        );

        ContractReview savedReview = contractReviewRepository.save(contractReview);
        log.info("Created contract review task: {} for contract: {}", task.getId(), contractId);

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
     * 直接启动合同审查（新的简化实现）
     * 此方法替代了复杂的多阶段管道处理，提供直接的合同审查功能。
     *
     * @param taskId 任务ID
     * @throws RuntimeException 当合同审查处理失败时
     */
    public void startContractReview(TaskId taskId) {
        log.info("Starting direct contract review for task: {}", taskId);

        ContractReview contractReview = getContractTask(taskId);

        try {
            // 启动任务
            taskManagementService.startTask(taskId);

            // 执行直接的合同审查处理（saveReviewResult会自动完成任务）
            executeDirectContractReview(contractReview);

            log.info("Direct contract review completed successfully for task: {}", taskId);

        } catch (Exception e) {
            log.error("Direct contract review failed for task: {}", taskId, e);
            taskManagementService.failTask(taskId, e.getMessage());
            throw new RuntimeException("Contract review processing failed", e);
        }
    }

    /**
     * 执行直接的合同审查处理
     * 这是简化的审查逻辑，替代了原有的多阶段管道处理。
     *
     * @param contractReview 合同审查对象
     */
    private void executeDirectContractReview(ContractReview contractReview) {
        log.info("Executing direct contract review for contract: {}", contractReview.getContractId());

        // 1. 获取合同信息
        // TODO: 调用正确的Feign客户端方法来获取合同信息
        // var contract = contractFeignClient.getContract(contractReview.getContractId());
        // 这里使用占位符实现，实际需要根据ContractFeignClient的API来调用

        // 2. 执行合同分析（这里可以集成AI服务或其他分析逻辑）
        ReviewResult reviewResult = performContractAnalysis(contractReview);

        // 3. 保存审查结果
        saveReviewResult(reviewResult);

        log.info("Contract analysis completed for task: {}", contractReview.getId());
    }

    /**
     * 执行合同分析
     *
     * @param contractReview 合同审查对象
     * @return 审查结果
     */
    private ReviewResult performContractAnalysis(ContractReview contractReview) {
        log.info("Performing contract analysis for contract: {}", contractReview.getContractId());

        // TODO: 集成实际的AI分析逻辑
        // 这里可以调用AI服务、规则引擎等进行分析

        // 创建审查结果
        ReviewResult reviewResult = ReviewResult.builder()
                .taskId(contractReview.getTaskId())
                .contractId(contractReview.getContractId())
                .reviewType("FULL_REVIEW")
                .overallRiskLevel("MEDIUM")
                .summary("Contract review completed using simplified direct processing")
                .build();

        // TODO: 根据ReviewResult的实际字段设置分析结果
        // 这里可能需要设置其他字段，如风险评分、合规评分等

        return reviewResult;
    }

    /**
     * 直接处理审查失败（新的简化实现）
     *
     * @param taskId 任务ID
     * @param errorMessage 错误信息
     */
    public void handleReviewFailureDirect(TaskId taskId, String errorMessage) {
        log.error("Direct contract review failed for task: {} - {}", taskId, errorMessage);
        taskManagementService.failTask(taskId, errorMessage);

        // 可以添加额外的失败处理逻辑，如通知、重试等
        // TODO: 实现失败通知逻辑
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
}