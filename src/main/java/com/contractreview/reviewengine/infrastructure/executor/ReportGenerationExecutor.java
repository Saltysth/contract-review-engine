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
 * 报告生成执行器
 * 负责处理报告生成阶段的任务
 *
 * @author SaltyFish
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReportGenerationExecutor {

    private final TaskRepository taskRepository;

    /**
     * 批量处理报告生成任务
     */
    @Transactional
    public void processBatch(List<Task> tasks) {
        if (tasks.isEmpty()) {
            return;
        }

        log.info("开始处理 {} 个报告生成任务", tasks.size());

        int successCount = 0;
        int failureCount = 0;

        for (Task task : tasks) {
            try {
                processSingleTask(task);
                successCount++;
                log.debug("任务 {} 报告生成处理成功", task.getId());

            } catch (Exception e) {
                handleTaskExecutionFailure(task, e);
                failureCount++;
                log.error("任务 {} 报告生成处理失败: {}", task.getId(), e.getMessage(), e);
            }
        }

        log.info("报告生成批次处理完成，成功 {} 个，失败 {} 个", successCount, failureCount);
    }

    /**
     * 处理单个报告生成任务
     */
    private void processSingleTask(Task task) {
        // 启动任务
        task.start();
        taskRepository.save(task);

        log.debug("开始执行任务 {} 的报告生成", task.getId());

        try {

            // 完成整个审查流程
            task.updateCurrentStage(ExecutionStage.REVIEW_COMPLETED);
            task.finish(); // 整个任务完成（程序执行成功）
            taskRepository.save(task);

            log.info("任务 {} 报告生成阶段完成，整个审查流程已结束", task.getId());

        } catch (Exception e) {
            // 程序执行失败，标记任务为失败状态以触发重试
            task.fail("报告生成程序执行失败: " + e.getMessage());
            taskRepository.save(task);
            throw e; // 重新抛出异常，让上层处理
        }
    }

    /**
     * 处理程序执行失败（需要重试的情况）
     * 报告生成说明：
     * - 基于之前的审查结果生成报告
     * - 风险合同 → 生成包含风险说明的报告
     * - 通过合同 → 生成正常报告
     * - 报告生成失败 → 程序异常，触发重试
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
     * 模拟报告生成过程
     * 实际实现中应该根据业务需求生成具体的报告内容
     */
    private Map<String, Object> simulateReportGeneration(Long contractId, Object reviewResult) {
        // 模拟返回最终审查报告
        return Map.of(
            "contractId", contractId,
            "reportId", "RPT_" + System.currentTimeMillis(),
            "reportType", "COMPREHENSIVE_REVIEW",
            "summary", Map.of(
                "overallRiskLevel", "MEDIUM",
                "overallRiskScore", 65.5,
                "complianceScore", 78.2,
                "totalReviewItems", 3,
                "highRiskItems", 0,
                "mediumRiskItems", 1,
                "lowRiskItems", 2
            ),
            "detailedAnalysis", Map.of(
                "clauseAnalysis", Map.of(
                    "totalClauses", 8,
                    "riskClauses", 2,
                    "compliantClauses", 6
                ),
                "complianceAnalysis", Map.of(
                    "overallCompliance", "ACCEPTABLE",
                    "criticalIssues", 0,
                    "recommendations", 2
                )
            ),
            "recommendations", List.of(
                "建议明确付款具体时间和方式",
                "建议添加违约责任的具体计算方法",
                "建议定期审查合同执行情况"
            ),
            "riskMitigation", List.of(
                "完善合同条款的明确性",
                "建立风险监控机制",
                "加强合规管理流程"
            ),
            "generatedTime", System.currentTimeMillis(),
            "status", "COMPLETED",
            "version", "1.0"
        );
    }
}