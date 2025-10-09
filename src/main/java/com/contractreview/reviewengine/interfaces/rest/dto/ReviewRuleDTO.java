package com.contractreview.reviewengine.interfaces.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author wangcong@SaltyFish
 * @description
 * @since 2025/10/09 20:44
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ReviewRuleDTO {
    @Schema(description = "规则名称")
    private String ruleName;

    @Schema(description = "规则描述")
    private String ruleDescription;

    @Schema(description = "是否启用")
    private boolean enabled;

    @Schema(description = "规则Tag")
    private List<String> tag;
}
