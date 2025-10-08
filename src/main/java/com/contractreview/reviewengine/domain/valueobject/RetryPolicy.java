package com.contractreview.reviewengine.domain.valueobject;

import lombok.Builder;
import lombok.Value;

/**
 * 重试策略值对象
 * 
 * @author SaltyFish
 */
@Value
@Builder
public class RetryPolicy {
    
    int maxRetries;
    long initialDelayMs;
    long maxDelayMs;
    double backoffMultiplier;
    boolean exponentialBackoff;
    
    // 兼容性字段
    public java.time.Duration getRetryInterval() {
        return java.time.Duration.ofMillis(initialDelayMs);
    }
    
    public RetryPolicy(int maxRetries, long initialDelayMs, long maxDelayMs, 
                      double backoffMultiplier, boolean exponentialBackoff) {
        if (maxRetries < 0) {
            throw new IllegalArgumentException("最大重试次数不能为负数");
        }
        if (initialDelayMs < 0) {
            throw new IllegalArgumentException("初始延迟时间不能为负数");
        }
        if (maxDelayMs < initialDelayMs) {
            throw new IllegalArgumentException("最大延迟时间不能小于初始延迟时间");
        }
        if (backoffMultiplier <= 0) {
            throw new IllegalArgumentException("退避倍数必须大于0");
        }
        
        this.maxRetries = maxRetries;
        this.initialDelayMs = initialDelayMs;
        this.maxDelayMs = maxDelayMs;
        this.backoffMultiplier = backoffMultiplier;
        this.exponentialBackoff = exponentialBackoff;
    }
    
    /**
     * 创建默认重试策略
     */
    public static RetryPolicy defaultPolicy() {
        return new RetryPolicy(3, 1000, 30000, 2.0, true);
    }
    
    /**
     * 创建无重试策略
     */
    public static RetryPolicy noRetry() {
        return new RetryPolicy(0, 0, 0, 1.0, false);
    }
    
    /**
     * 计算重试延迟时间
     */
    public long calculateDelay(int retryCount) {
        if (!exponentialBackoff) {
            return initialDelayMs;
        }
        
        long delay = (long) (initialDelayMs * Math.pow(backoffMultiplier, retryCount));
        return Math.min(delay, maxDelayMs);
    }
    
    /**
     * 检查是否可以重试
     */
    public boolean canRetry(int currentRetryCount) {
        return currentRetryCount < maxRetries;
    }
}