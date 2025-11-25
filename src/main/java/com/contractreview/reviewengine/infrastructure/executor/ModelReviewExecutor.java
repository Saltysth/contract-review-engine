package com.contractreview.reviewengine.infrastructure.executor;

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
 * 模型审查执行器
 * 负责处理AI模型审查阶段的任务
 *
 * @author SaltyFish
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ModelReviewExecutor {

    private final TaskRepository taskRepository;

    /**
     * 批量处理模型审查任务
     */
    @Transactional
    public void processBatch(List<Task> tasks) {
        if (tasks.isEmpty()) {
            return;
        }

        log.info("开始处理 {} 个模型审查任务", tasks.size());

        int successCount = 0;
        int failureCount = 0;

        for (Task task : tasks) {
            try {
                processSingleTask(task);
                successCount++;
                log.debug("任务 {} 模型审查处理成功", task.getId());

            } catch (Exception e) {
                handleTaskExecutionFailure(task, e);
                failureCount++;
                log.error("任务 {} 模型审查处理失败: {}", task.getId(), e.getMessage(), e);
            }
        }

        log.info("模型审查批次处理完成，成功 {} 个，失败 {} 个", successCount, failureCount);
    }

    /**
     * 处理单个模型审查任务
     */
    private void processSingleTask(Task task) {
        // 启动任务
        task.start();
        taskRepository.save(task);

        log.debug("开始执行任务 {} 的模型审查", task.getId());

        try {
            // 获取上一阶段的条款抽取结果
            Object extractionResult = getStageResult(task, ExecutionStage.CLAUSE_EXTRACTION);
            if (extractionResult == null) {
                throw new IllegalStateException("无法获取条款抽取结果，无法进行模型审查");
            }

            // 执行AI审查
            Map<String, Object> reviewResult = performAIReview(task, extractionResult);

            // 保存阶段结果（无论业务结果如何都保存）
            // 业务结果可能包含：风险等级、合规问题、通过/不通过等
            saveStageResult(task, reviewResult);

            // 更新到下一阶段
            task.updateCurrentStage(ExecutionStage.REPORT_GENERATION);
            task.complete(); // 当前阶段完成（程序执行成功）
            taskRepository.save(task);

            log.info("任务 {} 模型审查阶段完成，已推进到报告生成阶段", task.getId());

        } catch (Exception e) {
            // 程序执行失败，标记任务为失败状态以触发重试
            task.fail("模型审查程序执行失败: " + e.getMessage());
            taskRepository.save(task);
            throw e; // 重新抛出异常，让上层处理
        }
    }

    /**
     * 执行AI模型审查
     */
    private Map<String, Object> performAIReview(Task task, Object extractionResult) {
        try {
            Long contractId = getContractIdFromTask(task);
            if (contractId == null) {
                throw new IllegalArgumentException("无法获取合同ID");
            }

            log.debug("调用AI审查服务处理合同 {}", contractId);

            // 模拟AI服务调用
            // TODO: 实际实现中需要调用AI服务
            // ApiResponse<ChatResponse> response = aiClient.chat(buildChatRequest(extractionResult));
            // 处理AI响应...

            var aiReviewResult = simulateAIReview(contractId, extractionResult);

            log.debug("合同 {} AI审查完成，风险等级: {}", contractId, aiReviewResult.get("riskLevel"));

            return aiReviewResult;

        } catch (Exception e) {
            log.error("AI模型审查失败: {}", e.getMessage(), e);
            throw new RuntimeException("AI模型审查失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取指定阶段的结果
     */
    private Object getStageResult(Task task, ExecutionStage stage) {
        try {
            log.debug("获取任务 {} 的 {} 阶段结果", task.getId(), stage.name());
            // 这里需要根据实际的阶段结果查询逻辑来实现
            // 可以从review_result表中查询
            // 暂时返回模拟数据
            return Map.of(
                "stage", stage.name(),
                "contractId", getContractIdFromTask(task),
                "clauses", List.of(
                    Map.of("type", "支付条款", "content", "付款方式为月结"),
                    Map.of("type", "违约条款", "content", "逾期付款需支付违约金")
                )
            );
        } catch (Exception e) {
            log.warn("获取阶段结果失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 保存阶段结果
     */
    private void saveStageResult(Task task, Map<String, Object> result) {
        try {
            log.debug("保存任务 {} 的模型审查阶段结果", task.getId());
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
     * 业务结果说明：
     * - AI审查发现风险问题 → 保存结果，继续下一阶段（报告生成）
     * - AI审查通过 → 保存结果，继续下一阶段
     * - AI服务调用失败 → 程序异常，触发重试
     * - 网络错误 → 程序异常，触发重试
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
     * 模拟AI审查过程
     * 实际实现中应该调用真正的AI服务
     */
    private Map<String, Object> simulateAIReview(Long contractId, Object extractionResult) {
        // 模拟返回AI审查结果
        return Map.of(
            "contractId", contractId,
            "riskLevel", "MEDIUM",
            "riskScore", 65.5,
            "complianceScore", 78.2,
            "reviewItems", List.of(
                Map.of(
                    "category", "法律风险",
                    "severity", "MEDIUM",
                    "description", "付款条款存在歧义，建议明确具体付款时间"
                ),
                Map.of(
                    "category", "合规性",
                    "severity", "LOW",
                    "description", "合同格式符合标准要求"
                )
            ),
            "recommendations", List.of(
                "建议明确付款具体时间和方式",
                "建议添加违约责任的具体计算方法"
            ),
            "reviewTime", System.currentTimeMillis(),
            "status", "COMPLETED"
        );
    }
}