package com.contractreview.reviewengine.domain.valueobject;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author wangcong@SaltyFish
 * @description 模型审查结果反序列化类
 * @since 2025/11/30 12:09
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ModelReviewResult {
    private String riskLevel;
}
