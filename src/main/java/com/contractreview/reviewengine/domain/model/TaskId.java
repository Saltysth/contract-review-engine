package com.contractreview.reviewengine.domain.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;

import java.io.Serializable;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 任务ID值对象
 * 
 * @author SaltyFish
 */
@Value
@NoArgsConstructor(force = true)
public class TaskId implements Serializable {
    @NotBlank
    Long value;
    
    public TaskId(Long value) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("TaskId不能为空或小于等于0");
        }
        this.value = value;
    }
    
    /**
     * 生成新的任务ID
     */
    public static TaskId generate() {
        // 使用雪花算法或其他ID生成策略
        // 这里简化为随机数 + 时间戳
        long timestamp = System.currentTimeMillis();
        long random = ThreadLocalRandom.current().nextLong(1000, 9999);
        return new TaskId(timestamp * 10000 + random);
    }
    
    /**
     * 从字符串创建TaskId
     */
    public static TaskId of(Long value) {
        return new TaskId(value);
    }
    
    @Override
    public String toString() {
        return String.valueOf(value);
    }
}