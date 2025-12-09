package com.contractreview.reviewengine.domain.model;

import com.contractreview.reviewengine.domain.enums.RiskLevel;
import com.contractreview.reviewengine.domain.enums.RiskType;
import com.contractreview.reviewengine.domain.valueobject.Location;
import com.contractreview.reviewengine.infrastructure.converter.JsonConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 条款结果实体
 * 存储条款级别的审查结果
 *
 * @author SaltyFish
 */
@Entity
@Table(name = "clause_results")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClauseResultEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 关联的审查结果ID
     */
    @ManyToOne
    @JoinColumn(name = "review_result_id", nullable = false)
    private ReviewResult reviewResult;

    /**
     * 风险类型: risk/warning/info/suggestion
     */
    @Column(name = "risk_type", nullable = false)
    private RiskType riskType;

    /**
     * 内容描述
     */
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    /**
     * 风险等级
     */
    @Column(name = "risk_level")
    private RiskLevel riskLevel;

    /**
     * 相关审查规则ID列表（JSON格式存储）
     */
    @Column(name = "evidence_id", columnDefinition = "JSONB")
    @Convert(converter = JsonConverter.class)
    private List<Long> evidenceId;

    /**
     * 风险条款原文
     */
    @Column(name = "clause_text", columnDefinition = "TEXT")
    private String clauseText;

    /**
     * 条款位置信息（JSON格式存储）
     */
    @Column(name = "location", columnDefinition = "JSONB")
    @Convert(converter = JsonConverter.class)
    private Location location;
}