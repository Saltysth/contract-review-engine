package com.contractreview.reviewengine.infrastructure.executor;

import com.contract.common.constant.ExtractionStatus;
import com.contract.common.dto.TriggerClauseExtractionResponse;
import com.contract.common.feign.ClauseExtractionFeignClient;
import com.contractreview.reviewengine.domain.enums.ExecutionStage;
import com.contractreview.reviewengine.domain.enums.TaskStatus;
import com.contractreview.reviewengine.domain.model.ContractReview;
import com.contractreview.reviewengine.domain.model.Task;
import com.contractreview.reviewengine.domain.repository.TaskRepository;
import com.contractreview.reviewengine.infrastructure.service.ContractTaskInfraService;
import com.contractreview.reviewengine.interfaces.rest.dto.ContractTaskDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 条款抽取执行器
 * 负责处理合同条款抽取阶段的任务
 *
 * @author SaltyFish
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ClauseExtractionExecutor {

    private final TaskRepository taskRepository;
    private final ClauseExtractionFeignClient clauseExtractionFeignClient;
    private final ContractTaskInfraService contractTaskInfraService;
    private ContractReview contractTask;

    /**
     * 批量处理条款抽取任务
     */
    @Transactional
    public void processBatch(List<Task> tasks) {
        if (tasks.isEmpty()) {
            return;
        }

        log.info("开始处理 {} 个条款抽取任务", tasks.size());

        int successCount = 0;
        int failureCount = 0;

        for (Task task : tasks) {
            try {
                processSingleTask(task);
                successCount++;
                log.debug("任务 {} 条款抽取处理成功", task.getId());

            } catch (Exception e) {
                handleTaskExecutionFailure(task, e);
                failureCount++;
                log.error("任务 {} 条款抽取处理失败: {}", task.getId(), e.getMessage(), e);
            }
        }

        log.info("条款抽取批次处理完成，成功 {} 个，失败 {} 个", successCount, failureCount);
    }

    /**
     * 处理单个条款抽取任务
     */
    private void processSingleTask(Task task) {
        log.debug("开始执行任务 {} 的条款抽取", task.getId());

        try {
            contractTask = contractTaskInfraService.findContractTaskByTaskId(task.getId());

            // 执行条款抽取逻辑
            boolean result = performClauseExtraction(task);

            if (result) {
                // 更新到下一阶段
                task.updateCurrentStage(ExecutionStage.MODEL_REVIEW);

                log.info("任务 {} 条款抽取阶段完成，已推进到模型审查阶段", task.getId());
            }

            taskRepository.save(task);
        } catch (Exception e) {
            // 程序执行失败，标记任务为失败状态以触发重试
            task.fail("条款抽取程序执行失败: " + e.getMessage());
            taskRepository.save(task);
            throw e; // 重新抛出异常，让上层处理
        }
    }

    /**
     * 执行具体的条款抽取逻辑
     *
     * @return
     */
    private boolean performClauseExtraction(Task task) {
        try {
            // 获取合同信息
            Long contractId = contractTask.getContractId();
            if (contractId == null) {
                throw new IllegalArgumentException("无法获取合同ID");
            }

            if (task.getStatus().equals(TaskStatus.PENDING)) {
                // 启动任务
                task.start();
                // 调用外部服务进行条款抽取
                log.debug("调用条款抽取服务处理合同 {}", contractId);
            }

            TriggerClauseExtractionResponse extractionResult = clauseExtractionFeignClient.triggerClauseExtraction(contractId);

            log.debug("合同 {} 条款抽取状态为: {}，抽取到 {} 个条款", contractId, extractionResult.getExtractionStatus(), extractionResult.getExtractedClauseNumber());
            // TODO 现在的逻辑是有抽取任务进行中就不重新触发，后续可以加redis来加上次数标记，一定轮次后强制重试或者并日志报错。
            return extractionResult.getExtractionStatus().equals(ExtractionStatus.COMPLETED.name());
        } catch (Exception e) {
            log.error("条款抽取失败: {}", e.getMessage(), e);
            throw new RuntimeException("条款抽取失败: " + e.getMessage(), e);
        }
    }

    /**
     * 处理程序执行失败（需要重试的情况）
     * 区别于业务结果不通过（不需要重试）
     */
    private void handleTaskExecutionFailure(Task task, Exception e) {
        log.error("任务 {} 执行失败: {}", task.getId(), e.getMessage(), e);

        try {
            // 任务状态设为FAILED，触发重试机制
            task.fail("程序执行失败: " + e.getMessage());
            taskRepository.save(task);

        } catch (Exception saveException) {
            log.error("保存任务失败状态时发生异常: {}", saveException.getMessage());
            // 如果保存失败，记录日志但不抛出异常，避免影响其他任务处理
        }
    }


}