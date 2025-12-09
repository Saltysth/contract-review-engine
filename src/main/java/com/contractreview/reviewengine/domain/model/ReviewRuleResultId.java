package com.contractreview.reviewengine.domain.model;

import jakarta.validation.constraints.NotBlank;
import lombok.NoArgsConstructor;
import lombok.Value;

import java.io.Serializable;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 审查规则结果ID值对象
 *
 * @author SaltyFish
 */
@Value
@NoArgsConstructor(force = true)
public class ReviewRuleResultId implements Serializable {
    @NotBlank
    Long value;

    public ReviewRuleResultId(Long value) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("ReviewRuleResultId不能为空或小于等于0");
        }
        this.value = value;
    }

    /**
     * 生成新的审查规则结果ID
     */
    public static ReviewRuleResultId generate() {
        // 使用雪花算法或其他ID生成策略
        // 这里简化为随机数 + 时间戳
        long timestamp = System.currentTimeMillis();
        long random = ThreadLocalRandom.current().nextLong(1000, 9999);
        return new ReviewRuleResultId(timestamp * 10000 + random);
    }

    /**
     * 从字符串创建ReviewRuleResultId
     */
    public static ReviewRuleResultId of(Long value) {
        return new ReviewRuleResultId(value);
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}