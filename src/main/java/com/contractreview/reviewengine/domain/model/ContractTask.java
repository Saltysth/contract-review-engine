package com.contractreview.reviewengine.domain.model;

import com.contractreview.reviewengine.domain.enums.ReviewType;
import com.contractreview.reviewengine.domain.valueobject.AuditInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * 合同审查任务实体
 * 
 * @author SaltyFish
 */
@Entity
@Table(name = "contract_task", indexes = {
    @Index(name = "idx_contract_task_contract_id", columnList = "contract_id"),
    @Index(name = "idx_contract_task_review_type", columnList = "review_type"),
    @Index(name = "idx_contract_task_execution_stage", columnList = "execution_stage")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContractTask {
    
    @Id
    @Column(nullable = false)
    private Long id;

    @Column(name = "task_id", nullable = false)
    private Long taskId;

    @Column(name = "contract_id", nullable = false)
    private Long contractId;
    
    @Column(name = "file_uuid", nullable = false, length = 50)
    private String fileUuid;

    @Enumerated(EnumType.STRING)
    @Column(name = "review_type", nullable = false, length = 50)
    private ReviewType reviewType;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "review_rules", columnDefinition = "jsonb")
    private String reviewRules;
    
    @Column(name = "prompt_template", columnDefinition = "TEXT")
    private String promptTemplate;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "result_data", columnDefinition = "jsonb")
    private String resultData;
    
    @Column(name = "execution_stage", length = 50)
    private String executionStage = "PENDING";
    
    @Column(name = "progress_percentage")
    private Integer progressPercentage = 0;


    @Embedded
    private AuditInfo auditInfo;

    /**
     * 构造函数
     */
    public ContractTask(Long taskId, Long contractId, String fileUuid, ReviewType reviewType) {
        this.taskId = taskId;
        this.contractId = contractId;
        this.fileUuid = fileUuid;
        this.reviewType = reviewType;
        this.executionStage = "PENDING";
        this.progressPercentage = 0;
    }
    
    /**
     * 更新执行阶段
     */
    public void updateExecutionStage(String stage) {
        this.executionStage = stage;
    }
    
    /**
     * 更新进度百分比
     */
    public void updateProgress(Integer percentage) {
        this.progressPercentage = percentage;
    }
    
    /**
     * 更新审查规则
     */
    public void updateReviewRules(String rules) {
        this.reviewRules = rules;
    }
    
    /**
     * 更新提示模板
     */
    public void updatePromptTemplate(String template) {
        this.promptTemplate = template;
    }
    
    /**
     * 更新结果数据
     */
    public void updateResultData(String data) {
        this.resultData = data;
    }
    
    /**
     * 检查任务是否完成
     */
    public boolean isCompleted() {
        return "COMPLETED".equals(this.executionStage);
    }
    
    /**
     * 检查任务是否正在执行
     */
    public boolean isExecuting() {
        return "EXECUTING".equals(this.executionStage);
    }
    
    /**
     * 检查任务是否待处理
     */
    public boolean isPending() {
        return "PENDING".equals(this.executionStage);
    }
}