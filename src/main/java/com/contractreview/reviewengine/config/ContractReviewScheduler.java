package com.contractreview.reviewengine.config;

import com.contractreview.reviewengine.infrastructure.executor.ContractReviewAggregatorProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * 合同审查定时任务调度器
 * 负责定时触发状态聚合执行器处理任务
 *
 * @author SaltyFish
 */
@Slf4j
@Configuration
@EnableScheduling
@RequiredArgsConstructor
@ConditionalOnProperty(name = "contract.review.scheduler.enabled", havingValue = "true", matchIfMissing = true)
public class ContractReviewScheduler {

    private final ContractReviewAggregatorProcessor contractReviewAggregatorProcessor;

    /**
     * 主任务处理定时任务
     * 8秒轮询频率，处理非最终状态的任务
     */
    @Scheduled(fixedDelayString = "${contract.review.scheduler.process-delay:8000}")
    public void processTasks() {
        try {
            log.debug("开始执行定时任务处理");
            contractReviewAggregatorProcessor.processTasksByStage();
        } catch (Exception e) {
            log.error("定时任务处理失败", e);
        }
    }

    /**
     * 重试任务定时任务 TODO
     * 30秒检查重试频率，处理可重试的失败任务
     */
    @Scheduled(fixedDelayString = "${contract.review.scheduler.retry-delay:30000}")
    public void retryFailedTasks() {
        try {
            log.debug("重试检查未实现！开始执行重试任务检查");
//            contractReviewAggregatorProcessor.retryFailedTasks();
        } catch (Exception e) {
            log.error("重试任务处理失败", e);
        }
    }

    /**
     * 定时任务线程池配置
     */
    @Bean(name = "contractReviewTaskExecutor")
    public Executor contractReviewTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(10);
        executor.setThreadNamePrefix("contract-review-scheduler-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}