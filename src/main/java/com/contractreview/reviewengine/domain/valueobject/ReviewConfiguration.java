package com.contractreview.reviewengine.domain.valueobject;

import com.contractreview.reviewengine.domain.enums.PromptTemplateType;
import com.contractreview.reviewengine.domain.enums.ReviewType;
import com.contract.common.enums.ReviewTypeDetail;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 审查配置值对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewConfiguration {

    private boolean isDraft;

    private ReviewType reviewType;

    private List<ReviewTypeDetail> customSelectedReviewTypes;

    private String industry;

    private String currency;

    private String contractType;

    private BigDecimal typeConfidence;

    private List<String> reviewRules;

    /**
     * 会影响审查等速度和质量
     */
    private PromptTemplateType promptTemplate;

    private Boolean enableTerminology;

    /**
     * 创建默认配置
     */
    public static ReviewConfiguration defaultConfiguration() {
        return ReviewConfiguration.builder()
                .isDraft(true)
                .reviewType(ReviewType.FULL_REVIEW)
                .enableTerminology(true)
                .promptTemplate(PromptTemplateType.STANDARD)
                .build();
    }

    /**
     * 验证配置有效性
     */
    public boolean isValid() {
        return reviewType != null && promptTemplate != null;
    }

}