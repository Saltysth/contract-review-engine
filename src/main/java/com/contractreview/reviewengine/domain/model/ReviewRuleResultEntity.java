package com.contractreview.reviewengine.domain.model;

import com.contractreview.reviewengine.domain.enums.RiskLevel;
import com.contractreview.reviewengine.infrastructure.converter.JsonConverter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
 * 审查规则结果实体
 * 原本在ReviewResult中的riskItems字段拆分为独立表
 *
 * @author SaltyFish
 */
@Entity
@Table(name = "review_rule_results")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReviewRuleResultEntity {

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
     * 风险名称
     */
    @Column(name = "risk_name", nullable = false)
    private String riskName;

    /**
     * 审查规则类型
     */
    @Column(name = "rule_type", nullable = false)
    private String ruleType;

    /**
     * 风险等级
     * 0 无风险
     * [1~59] 低风险
     * [60~79] 中等风险
     * [80~89] 高风险
     * [90-100] 严重风险
     */
    @Column(name = "risk_level", nullable = false)
    @Enumerated(EnumType.STRING)
    private RiskLevel riskLevel;

    /**
     * 风险评分
     */
    @Column(name = "risk_score", columnDefinition = "NUMERIC")
    private Double riskScore;

    /**
     * 十字以内概要
     */
    @Column(name = "summary")
    private String summary;

    /**
     * 条款级别的结果（JSON格式存储）
     */
    @Column(name = "findings", columnDefinition = "JSONB")
    @Convert(converter = JsonConverter.class)
    private List<String> findings;

    /**
     * 操作建议（JSON格式存储）
     */
    @Column(name = "recommendation", columnDefinition = "JSONB")
    @Convert(converter = JsonConverter.class)
    private List<String> recommendation;

    /**
     * 风险条款ID
     */
    @Column(name = "risk_clause_id")
    private String riskClauseId;

    /**
     * 风险原文
     * 仅在风险条款ID为空的情况下存在，即非条款风险
     */
    @Column(name = "origin_contract_text", columnDefinition = "TEXT")
    private String originContractText;
}