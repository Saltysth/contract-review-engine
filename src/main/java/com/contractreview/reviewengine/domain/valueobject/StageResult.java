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
 */
@Value
@Builder
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