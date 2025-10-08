package com.contractreview.reviewengine.application.service;

import com.contractreview.reviewengine.domain.model.ContractTask;
import com.contractreview.reviewengine.domain.model.ReviewResult;
import com.contractreview.reviewengine.domain.model.TaskId;
import com.contractreview.reviewengine.domain.enums.ReviewType;
import com.contractreview.reviewengine.domain.enums.TaskType;
import com.contractreview.reviewengine.domain.repository.ContractTaskRepository;
import com.contractreview.reviewengine.domain.repository.ReviewResultRepository;
import com.contractreview.reviewengine.domain.valueobject.TaskConfiguration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 合同审查应用服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ContractReviewService {
    
    private final TaskService taskService;
    private final ContractTaskRepository contractTaskRepository;
    private final ReviewResultRepository reviewResultRepository;
    
    /**
     * 创建合同审查任务
     */
    public ContractTask createContractReviewTask(
            Long contractId,
            String filePath,
            String fileHash,
            ReviewType reviewType,
            TaskConfiguration configuration,
            Map<String, Object> metadata) {
        
        // TODO 检查是否已存在相同文件的任务
        Optional<ContractTask> existingTask = contractTaskRepository.findByFileUuid(filePath);
        if (existingTask.isPresent()) {
            log.info("Contract task already exists for file hash: {}", filePath);
            return existingTask.get();
        }
        
        // 创建基础任务
        var task = taskService.createTask(TaskType.CLASSIFICATION, configuration);
        
        // 创建合同任务
        ContractTask contractTask = new ContractTask();
        contractTask.setId(task.getId());
        contractTask.setContractId(contractId);
        contractTask.setFileUuid(filePath);
        contractTask.setReviewType(reviewType);

        ContractTask savedTask = contractTaskRepository.save(contractTask);
        log.info("Created contract review task: {} for contract: {}", task.getId(), contractId);
        
        return savedTask;
    }
    
    /**
     * 获取合同任务
     */
    @Transactional(readOnly = true)
    public ContractTask getContractTask(TaskId taskId) {
        return contractTaskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Contract task not found: " + taskId));
    }
    
    /**
     * 根据合同ID获取任务列表
     */
    @Transactional(readOnly = true)
    public List<ContractTask> getTasksByContractId(Long contractId) {
        return contractTaskRepository.findByContractId(contractId);
    }
    
    /**
     * 获取合同的最新任务
     */
    @Transactional(readOnly = true)
    public Optional<ContractTask> getLatestTaskByContractId(Long contractId) {
        List<ContractTask> tasks = contractTaskRepository.findLatestByContractId(contractId);
        return tasks.isEmpty() ? Optional.empty() : Optional.of(tasks.get(0));
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
        ContractTask contractTask = getContractTask(taskId);
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
}