package com.contractreview.reviewengine.application.service;

import com.contract.common.feign.ContractFeignClient;
import com.contract.common.feign.dto.ContractFeignDTO;
import com.contractreview.reviewengine.domain.model.ContractReview;
import com.contractreview.reviewengine.domain.model.ReviewResult;
import com.contractreview.reviewengine.domain.model.TaskId;
import com.contractreview.reviewengine.domain.enums.TaskType;
import com.contractreview.reviewengine.domain.repository.ContractReviewRepository;
import com.contractreview.reviewengine.domain.repository.ReviewResultRepository;
import com.contractreview.reviewengine.domain.valueobject.TaskConfiguration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
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

    private final TaskService taskService;
    private final ContractReviewRepository contractReviewRepository;
    private final ReviewResultRepository reviewResultRepository;
    
    /**
     * 创建合同审查任务
     */
    public ContractReview createContractReviewTask(Long contractId) {

        ContractFeignDTO contract = contractFeignClient.getContractById(contractId);

        // 创建基础任务
        var task = taskService.createTask(TaskType.CLASSIFICATION, TaskConfiguration.defaultTaskConfiguration());

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
        taskService.completeTask(reviewResult.getTaskId());
        
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
        taskService.startTask(taskId);

        // TODO: 发送消息到审查管道
        log.info("Started contract review for task: {}", taskId);
    }
    
    /**
     * 处理审查失败
     */
    public void handleReviewFailure(TaskId taskId, String errorMessage) {
        taskService.failTask(taskId, errorMessage);
        
        // TODO: 发送失败通知
        log.error("Contract review failed for task: {} - {}", taskId, errorMessage);
    }

    public Boolean deleteContractReviewTask(Long contractTaskId) {
        // 查询主数据的task

        // 删除task任务，外键会关联删除contractTask
//        int count = taskService.deleteTaskById(taskId);
//        return ONE == count;
        return true;
    }

    public ContractReview updateContractReviewTask(Long contractId) {
        return null;
    }
}