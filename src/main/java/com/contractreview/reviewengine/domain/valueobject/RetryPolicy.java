package com.contractreview.reviewengine.domain.valueobject;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 重试策略值对象
 * 
 * @author SaltyFish
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RetryPolicy {
    
    /**
     * 最大重试次数
     */
    @Builder.Default
    private Integer maxRetries = 3;
    
    /**
     * 重试间隔（毫秒）
     */
    @Builder.Default
    private Long retryIntervalMs = 1000L;


    /**
     * 已重试次数
     */
    @Builder.Default
    private Integer retryCount = 0;

    /**
     * 是否启用指数退避
     */
    @Builder.Default
    private Boolean exponentialBackoff = false;
    
    /**
     * 退避倍数
     */
    @Builder.Default
    private Double backoffMultiplier = 2.0;
    
    /**
     * 最大重试间隔（毫秒）
     */
    @Builder.Default
    private Long maxRetryIntervalMs = 30000L;

    /**
     * 下次重试时间
     */
    private LocalDateTime nextRetryTime;
    
    /**
     * 创建默认重试策略
     */
    public static RetryPolicy defaultPolicy() {
        return RetryPolicy.builder()
                .maxRetries(3)
                .retryCount(0)
                .retryIntervalMs(1000L)
                .exponentialBackoff(false)
                .backoffMultiplier(2.0)
                .maxRetryIntervalMs(30000L)
                .build();
    }
    
    /**
     * 创建无重试策略
     */
    public static RetryPolicy noRetry() {
        return RetryPolicy.builder()
                .maxRetries(0)
                .retryCount(0)
                .retryIntervalMs(0L)
                .exponentialBackoff(false)
                .build();
    }
    
    /**
     * 计算下一次重试间隔
     */
    public long calculateRetryInterval(int currentRetryCount) {
        if (!exponentialBackoff) {
            return retryIntervalMs;
        }
        
        long interval = (long) (retryIntervalMs * Math.pow(backoffMultiplier, currentRetryCount));
        return Math.min(interval, maxRetryIntervalMs);
    }
}