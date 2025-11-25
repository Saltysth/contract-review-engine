package com.contractreview.reviewengine.infrastructure.executor;

import com.contract.common.feign.ContractFeignClient;
import com.contractreview.reviewengine.domain.enums.ExecutionStage;
import com.contractreview.reviewengine.domain.model.Task;
import com.contractreview.reviewengine.domain.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

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
    private final ContractFeignClient contractFeignClient;

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
        // 启动任务
        task.start();
        taskRepository.save(task);

        log.debug("开始执行任务 {} 的条款抽取", task.getId());

        try {
            // 执行条款抽取逻辑
            performClauseExtraction(task);

            // 保存阶段结果（无论业务结果如何都保存）
            saveStageResult(task, Map.of(
                "stage", "CLAUSE_EXTRACTION",
                "status", "COMPLETED",
                "message", "条款抽取完成"
            ));

            // 更新到下一阶段
            task.updateCurrentStage(ExecutionStage.MODEL_REVIEW);
            task.complete(); // 当前阶段完成（程序执行成功）
            taskRepository.save(task);

            log.info("任务 {} 条款抽取阶段完成，已推进到模型审查阶段", task.getId());

        } catch (Exception e) {
            // 程序执行失败，标记任务为失败状态以触发重试
            task.fail("条款抽取程序执行失败: " + e.getMessage());
            taskRepository.save(task);
            throw e; // 重新抛出异常，让上层处理
        }
    }

    /**
     * 执行具体的条款抽取逻辑
     */
    private void performClauseExtraction(Task task) {
        try {
            // 获取合同信息
            Long contractId = getContractIdFromTask(task);
            if (contractId == null) {
                throw new IllegalArgumentException("无法获取合同ID");
            }

            // 调用外部服务进行条款抽取
            // 这里是示例逻辑，实际实现需要根据具体的AI服务接口调整
            log.debug("调用条款抽取服务处理合同 {}", contractId);

            // 模拟条款抽取处理
            // 实际应该调用AI服务或条款抽取服务
            var extractionResult = simulateClauseExtraction(contractId);

            log.debug("合同 {} 条款抽取完成，抽取到 {} 个条款", contractId, extractionResult.size());

        } catch (Exception e) {
            log.error("条款抽取失败: {}", e.getMessage(), e);
            throw new RuntimeException("条款抽取失败: " + e.getMessage(), e);
        }
    }

    /**
     * 保存阶段结果
     */
    private void saveStageResult(Task task, Object result) {
        try {
            log.debug("保存任务 {} 的条款抽取阶段结果", task.getId());
            // 这里需要根据实际的阶段结果存储逻辑来实现
            // 可以将结果存储到review_result表中
            // 暂时只记录日志
        } catch (Exception e) {
            log.warn("保存阶段结果失败: {}", e.getMessage());
            // 阶段结果保存失败不应该影响主流程
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

    /**
     * 从任务中获取合同ID
     */
    private Long getContractIdFromTask(Task task) {
        // 这里需要根据实际的Task结构来获取合同ID
        // 可能需要从Task的配置或其他字段中获取
        // 暂时返回一个示例值
        return 1L;
    }

    /**
     * 模拟条款抽取过程
     * 实际实现中应该调用真正的AI服务
     */
    private Map<String, Object> simulateClauseExtraction(Long contractId) {
        // 模拟返回条款抽取结果
        return Map.of(
            "contractId", contractId,
            "clauses", List.of(
                Map.of("type", "支付条款", "content", "付款方式为月结"),
                Map.of("type", "违约条款", "content", "逾期付款需支付违约金"),
                Map.of("type", "保密条款", "content", "双方需对合同内容保密")
            ),
            "extractionTime", System.currentTimeMillis()
        );
    }
}