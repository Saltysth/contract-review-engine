package com.contractreview.reviewengine.domain.valueobject;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 审计信息值对象
 * 
 * @author SaltyFish
 */
@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditInfo {

    @Column(name = "created_by")
    Long createdBy;
    
    @Column(name = "created_time")
    LocalDateTime createdTime;
    
    @Column(name = "updated_by")
    Long updatedBy;
    
    @Column(name = "updated_time")
    LocalDateTime updatedTime;

    @Column(name = "object_version_number")
    Long objectVersionNumber;
    
    public AuditInfo(Long createdBy, LocalDateTime createdTime, Long updatedBy, LocalDateTime updatedTime) {
        this.createdBy = createdBy;
        this.createdTime = createdTime != null ? createdTime : LocalDateTime.now();
        this.updatedBy = updatedBy;
        this.updatedTime = updatedTime != null ? updatedTime : LocalDateTime.now();
    }
    
    /**
     * 创建新的审计信息
     */
    public static AuditInfo create(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        return new AuditInfo(userId, now, userId, now);
    }
    
    /**
     * 更新审计信息
     */
    public AuditInfo update(Long userId) {
        return new AuditInfo(this.createdBy, this.createdTime, userId, LocalDateTime.now());
    }
}