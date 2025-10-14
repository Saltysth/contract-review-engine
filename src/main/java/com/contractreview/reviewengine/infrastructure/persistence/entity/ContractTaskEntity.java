package com.contractreview.reviewengine.infrastructure.persistence.entity;

import com.contractreview.reviewengine.domain.enums.PromptTemplateType;
import com.contractreview.reviewengine.domain.enums.ReviewType;
import com.contractreview.reviewengine.domain.enums.ReviewTypeDetail;
import com.contractreview.reviewengine.domain.valueobject.AuditInfo;
import com.contractreview.reviewengine.infrastructure.converter.StringListConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.List;

/**
 * 合同审查任务JPA实体
 *
 * @author SaltyFish
 */
@Entity
@Table(name = "contract_task", indexes = {
    @Index(name = "idx_contract_task_contract_id", columnList = "contract_id"),
    @Index(name = "idx_contract_task_review_type", columnList = "review_type")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContractTaskEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    @Column(name = "task_id", nullable = false)
    private Long taskId;

    @Column(name = "contract_id", nullable = false)
    private Long contractId;

    @Column(name = "file_uuid", nullable = false, length = 50)
    private String fileUuid;

    @Column(name = "contract_title", nullable = false)
    private String contractTitle;

    @Convert(converter = StringListConverter.class)
    @Column(name = "business_tags")
    private List<String> businessTags;

    @Enumerated(EnumType.STRING)
    @Column(name = "review_type", length = 50)
    private ReviewType reviewType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "custom_selected_review_types", columnDefinition = "jsonb")
    List<ReviewTypeDetail> customSelectedReviewTypes;

    @Column(name = "industry", length = 50)
    private String industry;

    @Column(name = "currency", length = 50)
    private String currency;

    @Column(name = "contract_type", length = 50)
    private String contractType;

    @Column(name = "type_confidence", precision = 5, scale = 2)
    private BigDecimal typeConfidence;

    @Column(name = "review_rules", columnDefinition = "TEXT")
    private String reviewRules;

    @Enumerated(EnumType.STRING)
    @Column(name = "prompt_template", length = 20)
    private PromptTemplateType promptTemplate;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "result_data", columnDefinition = "jsonb")
    private String resultData;

    @Column(name = "enable_terminology")
    private Boolean enableTerminology;

    @Embedded
    private AuditInfo auditInfo;

    /**
     * 构造函数
     */
    public ContractTaskEntity(Long taskId, Long contractId, String fileUuid, ReviewType reviewType) {
        this.taskId = taskId;
        this.contractId = contractId;
        this.fileUuid = fileUuid;
        this.reviewType = reviewType;
    }
}