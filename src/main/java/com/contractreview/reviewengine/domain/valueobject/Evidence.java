package com.contractreview.reviewengine.domain.valueobject;

import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 证据信息
 * 用于存储条款审查的证据来源和详细内容
 *
 * @author SaltyFish
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Evidence {

    /**
     * 证据标题
     */
    private String title;

    /**
     * 证据类型: rule/knowledge
     * rule: 规则库证据
     * knowledge: 知识库证据
     */
    private EvidenceType type;

    /**
     * 证据详细内容
     */
    private String content;

    /**
     * 行业（适用于基准数据类型）
     */
    private String industry;

    /**
     * 参考文献或法条（适用于知识库类型）
     */
    private List<String> references;

    @Getter
    @AllArgsConstructor
    public enum EvidenceType {
        RULE("rule", "规则库证据"),
        KNOWLEDGE("knowledge", "知识库证据");

        private final String type;
        private final String description;
    }
}