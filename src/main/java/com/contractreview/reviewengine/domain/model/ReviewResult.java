package com.contractreview.reviewengine.domain.model;

import com.contractreview.reviewengine.domain.valueobject.Evidence;
import com.contractreview.reviewengine.domain.valueobject.KeyPoint;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 审查结果实体
 * 对应数据库表 review_result
 *
 * @author SaltyFish
 */
@Entity
@Table(name = "review_result")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReviewResult {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "task_id", nullable = false)
    private Long taskId;
    
    @Column(name = "contract_id", nullable = false)
    private Long contractId;

    @Column(name = "review_type", nullable = false, length = 50)
    private String reviewType;

    @Column(name = "overall_risk_level", length = 20)
    private String overallRiskLevel;

    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;

    @Column(name = "stage_result", columnDefinition = "TEXT")
    private String stageResult;

    /**
     * 模型版本
     * 存储用于审查的AI模型版本号
     */
    @Column(name = "model_version", length = 50)
    private String modelVersion;

    /**
     * 审查规则结果列表（拆分为独立的子表）
     */
    @OneToMany(mappedBy = "reviewResult", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ReviewRuleResultEntity> ruleResults;

    /**
     * 关键点列表（包含关键点和修复建议）
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "key_points")
    private List<KeyPoint> keyPoints;

    /**
     * 证据列表
     * 存储审查过程中使用的证据信息
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "evidences")
    private List<Evidence> evidences;

    @Column(name = "created_time", nullable = false, updatable = false)
    private LocalDateTime createdTime;
    
    @PrePersist
    protected void onCreate() {
        if (createdTime == null) {
            createdTime = LocalDateTime.now();
        }
    }


}