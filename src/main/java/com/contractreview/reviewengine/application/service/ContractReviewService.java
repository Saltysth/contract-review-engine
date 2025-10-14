package com.contractreview.reviewengine.application.service;

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
import com.contractreview.reviewengine.interfaces.rest.dto.ContractReviewRequestDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    
    /**
     * 创建合同审查任务
     */
    public ContractReview createContractReviewTask(Long contractId) {

        ContractFeignDTO contract = contractFeignClient.getContractById(contractId);

        // 创建基础任务
        var task = taskManagementService.createTask(TaskType.CLASSIFICATION, TaskConfiguration.defaultTaskConfiguration());

        // 创建合同审查
        ContractReview contractReview = ContractReview.create(
                task.getId(),
                contractId,
                contract.getAttachmentUuid(),
                contract.getContractName()
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
     * 启动合同审查
     */
    public void startContractReview(TaskId taskId) {
        ContractReview contractReview = getContractTask(taskId);
        taskManagementService.startTask(taskId);

        // TODO: 发送消息到审查管道
        log.info("Started contract review for task: {}", taskId);
    }

    /**
     * 处理审查失败
     */
    public void handleReviewFailure(TaskId taskId, String errorMessage) {
        taskManagementService.failTask(taskId, errorMessage);

        // TODO: 发送失败通知
        log.error("Contract review failed for task: {} - {}", taskId, errorMessage);
    }

    public Boolean deleteContractReviewTask(Long contractTaskId) {
        // 查询主数据的task
        Optional<ContractReview> contractReview = contractReviewRepository.findById(TaskId.of(contractTaskId));

        // 如果不存在，直接返回false
        if (contractReview.isEmpty()) {
            return false;
        }

        // 如果存在，执行删除并返回删除结果
        ContractReview mainTask = contractReview.get();
        return taskManagementService.deleteTask(TaskId.of(mainTask.getTaskId()));
    }

    public ContractReview updateContractReviewTask(Long contractId, @Valid ContractReviewRequestDto requestDto) {

        return null;
    }
}