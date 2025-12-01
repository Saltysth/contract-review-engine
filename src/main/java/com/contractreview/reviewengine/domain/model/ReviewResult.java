package com.contractreview.reviewengine.domain.model;

import com.contractreview.reviewengine.infrastructure.converter.JsonConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.contractreview.reviewengine.domain.valueobject.RiskItem;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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

    @Column(name = "confidence", precision = 5, scale = 2)
    private BigDecimal confidence;
    
    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;
    
    @Column(name = "recommendations", columnDefinition = "TEXT")
    private String recommendations;

    @Column(name = "stage_result", columnDefinition = "TEXT")
    private String stageResult;

    @Column(name = "risk_items", columnDefinition = "JSONB")
    @Convert(converter = JsonConverter.class)
    private List<RiskItem> riskItems;

    @Column(name = "compliance_issues", columnDefinition = "JSONB")
    @Convert(converter = JsonConverter.class)
    private Map<String, Object> complianceIssues;
    
    @Column(name = "created_time", nullable = false, updatable = false)
    private LocalDateTime createdTime;
    
    @PrePersist
    protected void onCreate() {
        if (createdTime == null) {
            createdTime = LocalDateTime.now();
        }
    }
    
    /**
     * 创建审查结果
     */
    public static ReviewResult create(Long taskId, Long contractId, String reviewType) {
        return ReviewResult.builder()
                .taskId(taskId)
                .contractId(contractId)
                .reviewType(reviewType)
                .createdTime(LocalDateTime.now())
                .build();
    }
}