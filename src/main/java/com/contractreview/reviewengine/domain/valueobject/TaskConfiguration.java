package com.contractreview.reviewengine.domain.valueobject;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * 任务配置值对象
 * 
 * @author SaltyFish
 */
@Data
@Builder
@NoArgsConstructor(force = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TaskConfiguration {
    @Schema(description = "重试策略")
    RetryPolicy retryPolicy;
    @Schema(description = "超时时间（秒）")
    Integer timeoutSeconds;
    @Schema(description = "优先级")
    Integer priority;
    @Schema(description = "并发数")
    Integer concurrency;
    @Schema(description = "是否为草稿状态")
    Boolean isDraft;
    @Schema(description = "自定义设置")
    Map<String, Object> customSettings;
    
    public TaskConfiguration(RetryPolicy retryPolicy,
                           Integer timeoutSeconds,
                           Integer priority,
                           Integer concurrency,
                           Boolean isDraft,
                           Map<String, Object> customSettings) {
        this.retryPolicy = retryPolicy != null ? retryPolicy : RetryPolicy.defaultPolicy();
        this.timeoutSeconds = timeoutSeconds != null ? timeoutSeconds : 3600;
        this.priority = priority != null ? priority : 0;
        this.customSettings = customSettings != null ? new HashMap<>(customSettings) : new HashMap<>();
        this.concurrency = concurrency != null ? concurrency : 0;
        this.isDraft = isDraft;
    }

    public static TaskConfiguration defaultTaskConfiguration() {
        return TaskConfiguration.builder()
                .retryPolicy(RetryPolicy.defaultPolicy())
                .isDraft(true)
                .timeoutSeconds(3600)
                .priority(0)
                .customSettings(new HashMap<>())
                .build();
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
    @com.fasterxml.jackson.annotation.JsonIgnore
    public java.time.Duration getTimeout() {
        return java.time.Duration.ofSeconds(timeoutSeconds);
    }
}