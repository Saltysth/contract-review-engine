package com.contractreview.reviewengine.domain.model;

import com.contractreview.reviewengine.domain.enums.ExecutionStage;
import com.contractreview.reviewengine.domain.enums.ReviewType;
import com.contractreview.reviewengine.domain.valueobject.AuditInfo;
import com.contractreview.reviewengine.domain.valueobject.RiskAssessment;
import com.contractreview.reviewengine.infrastructure.converter.JsonConverter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 审查结果实体
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
    
    @Enumerated(EnumType.STRING)
    @Column(name = "review_type", nullable = false)
    private ReviewType reviewType;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "current_stage")
    private ExecutionStage currentStage;
    
    @Column(name = "extracted_clauses", columnDefinition = "JSONB")
    @Convert(converter = JsonConverter.class)
    private List<Map<String, Object>> extractedClauses;
    
    @Column(name = "analysis_result", columnDefinition = "JSONB")
    @Convert(converter = JsonConverter.class)
    private Map<String, Object> analysisResult;
    
    @Column(name = "risk_assessment", columnDefinition = "JSONB")
    @Convert(converter = JsonConverter.class)
    private RiskAssessment riskAssessment;
    
    @Column(name = "recommendations", columnDefinition = "JSONB")
    @Convert(converter = JsonConverter.class)
    private List<String> recommendations;
    
    @Column(name = "confidence_score")
    private Double confidenceScore;
    
    @Column(name = "processing_time_ms")
    private Long processingTimeMs;
    
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "createdBy", column = @Column(name = "created_by")),
        @AttributeOverride(name = "createdAt", column = @Column(name = "created_at", nullable = false)),
        @AttributeOverride(name = "updatedBy", column = @Column(name = "updated_by")),
        @AttributeOverride(name = "updatedAt", column = @Column(name = "updated_at"))
    })
    private AuditInfo auditInfo;
    
    public ReviewResult(Long taskId, ReviewType reviewType, Long createdBy) {
        this.taskId = taskId;
        this.reviewType = reviewType;
        this.auditInfo = AuditInfo.create(createdBy);
    }
    
    /**
     * 初始化审计信息
     */
    public void initializeAuditInfo() {
        if (this.auditInfo == null) {
            this.auditInfo = AuditInfo.create(getCurrentUserId());
        }
    }
    
    /**
     * 获取当前阶段
     */
    public ExecutionStage getCurrentStage() {
        return this.currentStage;
    }
    
    /**
     * 设置当前阶段
     */
    public void setCurrentStage(ExecutionStage stage) {
        this.currentStage = stage;
        updateAuditInfo();
    }
    
    /**
     * 获取提取的条款
     */
    public List<Map<String, Object>> getExtractedClauses() {
        return this.extractedClauses;
    }
    
    /**
     * 设置提取的条款
     */
    public void setExtractedClauses(List<Map<String, Object>> clauses) {
        this.extractedClauses = clauses;
        updateAuditInfo();
    }
    
    /**
     * 获取分析结果
     */
    public Map<String, Object> getAnalysisResult() {
        return this.analysisResult;
    }
    
    /**
     * 设置分析结果
     */
    public void setAnalysisResult(Map<String, Object> result) {
        this.analysisResult = result;
        updateAuditInfo();
    }
    
    /**
     * 获取审计信息
     */
    public AuditInfo getAuditInfo() {
        return this.auditInfo;
    }
    
    /**
     * 设置审计信息
     */
    public void setAuditInfo(AuditInfo auditInfo) {
        this.auditInfo = auditInfo;
    }
    
    /**
     * 更新风险评估
     */
    public void updateRiskAssessment(RiskAssessment assessment) {
        this.riskAssessment = assessment;
        updateAuditInfo();
    }
    
    /**
     * 添加推荐建议
     */
    public void addRecommendation(String recommendation) {
        if (this.recommendations == null) {
            this.recommendations = new java.util.ArrayList<>();
        }
        this.recommendations.add(recommendation);
        updateAuditInfo();
    }
    
    /**
     * 设置置信度分数
     */
    public void setConfidenceScore(Double score) {
        this.confidenceScore = score;
        updateAuditInfo();
    }
    
    /**
     * 设置处理时间
     */
    public void setProcessingTime(Long timeMs) {
        this.processingTimeMs = timeMs;
        updateAuditInfo();
    }
    
    /**
     * 更新审计信息
     */
    private void updateAuditInfo() {
        if (this.auditInfo != null) {
            this.auditInfo = this.auditInfo.update(getCurrentUserId());
        }
    }
    
    /**
     * 获取当前用户ID
     */
    private Long getCurrentUserId() {
        // TODO: 从SecurityContext获取当前用户ID
        return 1L;
    }
}