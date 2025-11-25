package com.contractreview.reviewengine.domain.valueobject;

import com.contractreview.reviewengine.domain.enums.ExecutionStage;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 阶段执行结果值对象
 *
 * @author SaltyFish
 * @deprecated 此值对象已被弃用。管道阶段处理方式将被简化的直接处理方式替代。
 * 此值对象表示单个管道阶段的执行结果，在简化的处理方式中不再需要。
 * 迁移指南：使用 {@link ReviewResult} 直接存储审查结果，无需阶段性结果。
 * @since 1.0.0
 * @see com.contractreview.reviewengine.domain.model.ReviewResult
 */
@Value
@Builder
@Deprecated(since = "1.0.0", forRemoval = true)
public class StageResult {
    
    ExecutionStage stage;
    boolean success;
    String output;
    String errorMessage;
    LocalDateTime startTime;
    LocalDateTime endTime;
    @Builder.Default
    Map<String, Object> data = new HashMap<>();
    
    /**
     * 获取数据值
     */
    public Object getData(String key) {
        return data.get(key);
    }
    
    /**
     * 检查是否包含数据
     */
    public boolean hasData(String key) {
        return data.containsKey(key);
    }
}