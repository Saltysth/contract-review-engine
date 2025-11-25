package com.contractreview.reviewengine.infrastructure.executor;

import com.contractreview.reviewengine.domain.enums.ExecutionStage;
import com.contractreview.reviewengine.domain.enums.TaskStatus;
import com.contractreview.reviewengine.domain.model.Task;
import com.contractreview.reviewengine.domain.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 合同审查状态聚合执行器
 * 负责获取所有非最终状态任务，按阶段分组后调用专门执行器
 *
 * @author SaltyFish
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ContractReviewAggregatorProcessor {

    private final TaskRepository taskRepository;
    private final ClauseExtractionExecutor clauseExtractionExecutor;
    private final ModelReviewExecutor modelReviewExecutor;
    private final ReportGenerationExecutor reportGenerationExecutor;

    /**
     * 按阶段批量处理任务
     */
    @Transactional
    public void processTasksByStage() {
        try {
            log.debug("开始获取非最终状态任务进行聚合处理");

            // 查询所有非最终状态的任务
            List<Task> nonFinalTasks = taskRepository.findNonFinalStageTasks();

            if (nonFinalTasks.isEmpty()) {
                log.debug("当前没有待处理的任务");
                return;
            }

            log.info("获取到 {} 个非最终状态任务，开始按阶段聚合处理", nonFinalTasks.size());

            // 按阶段聚合任务
            Map<ExecutionStage, List<Task>> tasksByStage = nonFinalTasks.stream()
                .collect(Collectors.groupingBy(Task::getCurrentStage));

            log.info("任务阶段分布: {}",
                tasksByStage.entrySet().stream()
                    .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().size()
                    )));

            // 批量处理各阶段任务
            processBatchTasks(tasksByStage);

        } catch (Exception e) {
            log.error("状态聚合处理失败", e);
            throw e;
        }
    }

    /**
     * 批量处理各阶段任务
     */
    private void processBatchTasks(Map<ExecutionStage, List<Task>> tasksByStage) {
        for (Map.Entry<ExecutionStage, List<Task>> entry : tasksByStage.entrySet()) {
            ExecutionStage stage = entry.getKey();
            List<Task> tasks = entry.getValue();

            if (tasks.isEmpty()) {
                continue;
            }

            try {
                log.info("开始处理阶段 {} 的 {} 个任务", stage.getDisplayName(), tasks.size());

                switch (stage) {
                    case CLAUSE_EXTRACTION:
                        clauseExtractionExecutor.processBatch(tasks);
                        break;
                    case MODEL_REVIEW:
                        modelReviewExecutor.processBatch(tasks);
                        break;
                    case REPORT_GENERATION:
                        reportGenerationExecutor.processBatch(tasks);
                        break;
                    case CONTRACT_CLASSIFICATION:
                    case KNOWLEDGE_MATCHING:
                    case RESULT_VALIDATION:
                    case REVIEW_COMPLETED:
                        log.debug("阶段 {} 暂未实现，跳过处理", stage.getDisplayName());
                        break;
                }

                log.info("阶段 {} 处理完成", stage.getDisplayName());

            } catch (Exception e) {
                log.error("处理阶段 {} 时发生错误: {}", stage.getDisplayName(), e.getMessage(), e);
                // 阶段处理异常，记录但不中断其他阶段
            }
        }
    }

    /**
     * 重试执行失败的任务
     * 重试条件：程序执行失败（异常、网络错误等），而不是业务结果不通过
     */
    @Transactional
    public void retryFailedTasks() {
        try {
            log.debug("开始检查可重试的失败任务");

            List<Task> retryableTasks = taskRepository.findRetryableTasks();

            if (retryableTasks.isEmpty()) {
                log.debug("当前没有需要重试的任务");
                return;
            }

            log.info("获取到 {} 个可重试任务，开始重试处理", retryableTasks.size());

            int successRetryCount = 0;
            int failedRetryCount = 0;

            for (Task task : retryableTasks) {
                try {
                    if (!canRetry(task)) {
                        log.warn("任务 {} 已达到最大重试次数，标记为永久失败", task.getId());
                        task.fail("Max retry count exceeded");
                        taskRepository.save(task);
                        failedRetryCount++;
                        continue;
                    }

                    // 执行重试逻辑
                    task.retry(); // 重置为PENDING状态
                    taskRepository.save(task);

                    successRetryCount++;
                    log.info("任务 {} 已重置为重试状态，第 {} 次重试",
                        task.getId(), task.getConfiguration().getRetryPolicy().getRetryCount() + 1);

                } catch (Exception e) {
                    log.error("任务 {} 重试失败: {}", task.getId(), e.getMessage(), e);
                    // 重试失败，增加重试计数
                    incrementRetryCount(task);
                    taskRepository.save(task);
                    failedRetryCount++;
                }
            }

            log.info("重试处理完成，成功重置 {} 个任务，失败 {} 个任务", successRetryCount, failedRetryCount);

        } catch (Exception e) {
            log.error("重试处理失败", e);
            throw e;
        }
    }

    /**
     * 检查任务是否可以重试
     */
    private boolean canRetry(Task task) {
        if (task.getConfiguration() == null || task.getConfiguration().getRetryPolicy() == null) {
            return true; // 如果没有配置，默认允许重试
        }

        return task.getConfiguration().getRetryPolicy().getRetryCount() <
               task.getConfiguration().getRetryPolicy().getMaxRetries();
    }

    /**
     * 增加重试计数并计算下次重试时间
     */
    private void incrementRetryCount(Task task) {
        if (task.getConfiguration() == null || task.getConfiguration().getRetryPolicy() == null) {
            return;
        }

        var retryPolicy = task.getConfiguration().getRetryPolicy();
        retryPolicy.setRetryCount(retryPolicy.getRetryCount() + 1);

        // 计算下次重试时间
        long nextRetryInterval = retryPolicy.calculateRetryInterval(retryPolicy.getRetryCount());
        LocalDateTime nextRetryTime = LocalDateTime.now().plusSeconds(nextRetryInterval / 1000);
        retryPolicy.setNextRetryTime(nextRetryTime);

        log.debug("任务 {} 重试计数增加到 {}, 下次重试时间: {}",
            task.getId(), retryPolicy.getRetryCount(), nextRetryTime);
    }
}