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
            // 获取AI审查结果
            Object reviewResult = getStageResult(task, ExecutionStage.MODEL_REVIEW);
            if (reviewResult == null) {
                throw new IllegalStateException("无法获取模型审查结果，无法生成报告");
            }

            // 生成最终报告（基于之前的审查结果，无论风险等级如何）
            Map<String, Object> finalReport = generateFinalReport(task, reviewResult);

            // 保存最终结果
            saveFinalResult(task, finalReport);

            // 完成整个审查流程
            task.updateCurrentStage(ExecutionStage.REVIEW_COMPLETED);
            task.complete(); // 整个任务完成（程序执行成功）
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
     * 生成最终报告
     */
    private Map<String, Object> generateFinalReport(Task task, Object reviewResult) {
        try {
            Long contractId = getContractIdFromTask(task);
            if (contractId == null) {
                throw new IllegalArgumentException("无法获取合同ID");
            }

            log.debug("开始生成合同 {} 的最终审查报告", contractId);

            // 生成报告的逻辑
            // 这里是示例逻辑，实际实现需要根据具体的报告生成需求调整
            var finalReport = simulateReportGeneration(contractId, reviewResult);

            log.debug("合同 {} 最终报告生成完成", contractId);

            return finalReport;

        } catch (Exception e) {
            log.error("生成最终报告失败: {}", e.getMessage(), e);
            throw new RuntimeException("生成最终报告失败: " + e.getMessage(), e);
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
                "contractId", getContractIdFromTask(task),
                "riskLevel", "MEDIUM",
                "riskScore", 65.5,
                "complianceScore", 78.2,
                "reviewItems", List.of(
                    Map.of(
                        "category", "法律风险",
                        "severity", "MEDIUM",
                        "description", "付款条款存在歧义"
                    )
                )
            );
        } catch (Exception e) {
            log.warn("获取阶段结果失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 保存最终结果
     */
    private void saveFinalResult(Task task, Map<String, Object> finalReport) {
        try {
            log.debug("保存任务 {} 的最终审查结果", task.getId());
            // 这里需要根据实际的审查结果存储逻辑来实现
            // 可以将结果存储到review_result表中
            // 暂时只记录日志
        } catch (Exception e) {
            log.warn("保存最终结果失败: {}", e.getMessage());
            // 最终结果保存失败不应该影响主流程
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