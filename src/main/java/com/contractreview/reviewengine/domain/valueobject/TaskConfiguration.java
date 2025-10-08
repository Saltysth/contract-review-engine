package com.contractreview.reviewengine.domain.valueobject;

import lombok.Builder;
import lombok.Value;

import java.util.HashMap;
import java.util.Map;

/**
 * 任务配置值对象
 * 
 * @author SaltyFish
 */
@Value
@Builder
public class TaskConfiguration {
    
    Map<String, Object> reviewRules;
    String promptTemplate;
    RetryPolicy retryPolicy;
    Integer timeoutSeconds;
    Map<String, Object> customSettings;
    
    public TaskConfiguration(Map<String, Object> reviewRules, 
                           String promptTemplate,
                           RetryPolicy retryPolicy,
                           Integer timeoutSeconds,
                           Map<String, Object> customSettings) {
        this.reviewRules = reviewRules != null ? new HashMap<>(reviewRules) : new HashMap<>();
        this.promptTemplate = promptTemplate;
        this.retryPolicy = retryPolicy != null ? retryPolicy : RetryPolicy.defaultPolicy();
        this.timeoutSeconds = timeoutSeconds != null ? timeoutSeconds : 3600;
        this.customSettings = customSettings != null ? new HashMap<>(customSettings) : new HashMap<>();
    }
    
    /**
     * 获取规则值
     */
    public Object getRuleValue(String ruleKey) {
        return reviewRules.get(ruleKey);
    }
    
    /**
     * 检查是否包含规则
     */
    public boolean hasRule(String ruleKey) {
        return reviewRules.containsKey(ruleKey);
    }
    
    /**
     * 获取自定义设置值
     */
    public Object getCustomSetting(String key) {
        return customSettings.get(key);
    }
    
    /**
     * 检查是否包含自定义设置
     */
    public boolean hasCustomSetting(String key) {
        return customSettings.containsKey(key);
    }
    
    /**
     * 获取超时时间（兼容性方法）
     */
    public java.time.Duration getTimeout() {
        return java.time.Duration.ofSeconds(timeoutSeconds);
    }
}