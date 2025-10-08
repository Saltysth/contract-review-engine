package com.contractreview.reviewengine.domain.model;

import com.contractreview.reviewengine.domain.enums.TaskType;
import com.contractreview.reviewengine.domain.enums.ReviewType;
import com.contractreview.reviewengine.domain.valueobject.TaskConfiguration;
import com.contractreview.reviewengine.domain.valueobject.ReviewProgress;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import jakarta.persistence.*;

/**
 * 合同审查任务实体
 * 
 * @author SaltyFish
 */
@Entity
@Table(name = "contract_task")
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class ContractTask extends Task {
    
    @Column(name = "contract_id", nullable = false)
    private String contractId;
    
    @Column(name = "file_path", nullable = false)
    private String filePath;
    
    @Column(name = "file_hash")
    private String fileHash;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "review_type", nullable = false)
    private ReviewType reviewType;
    
    @Column(name = "file_size")
    private Long fileSize;
    
    @Column(name = "file_name")
    private String fileName;
    
    public ContractTask(String taskName, String contractId, String filePath, ReviewType reviewType, Long createdBy) {
        super(taskName, TaskType.CONTRACT_REVIEW, 0, createdBy);
        this.contractId = contractId;
        this.filePath = filePath;
        this.reviewType = reviewType;
    }
    
    /**
     * 更新配置
     */
    public void updateConfiguration(TaskConfiguration newConfiguration) {
        this.setConfiguration(newConfiguration);
        updateAuditInfo();
    }
    
    /**
     * 更新进度
     */
    public void updateReviewProgress(ReviewProgress newProgress) {
        this.setProgress(newProgress);
        updateAuditInfo();
    }
    
    /**
     * 检查是否可以执行
     */
    public boolean canExecute() {
        return getStatus().canExecute();
    }
    
    /**
     * 获取文件路径
     */
    public String getFilePath() {
        return this.filePath;
    }
    
    /**
     * 获取文件哈希
     */
    public String getFileHash() {
        return this.fileHash;
    }
    
    /**
     * 设置文件哈希
     */
    public void setFileHash(String fileHash) {
        this.fileHash = fileHash;
        updateAuditInfo();
    }
    
    /**
     * 设置文件大小
     */
    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
        updateAuditInfo();
    }
    
    /**
     * 设置文件名
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
        updateAuditInfo();
    }
    
    /**
     * 更新审计信息
     */
    private void updateAuditInfo() {
        if (getAuditInfo() != null) {
            setAuditInfo(getAuditInfo().update(getCurrentUserId()));
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