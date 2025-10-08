package com.contractreview.reviewengine.domain.valueobject;

import lombok.Value;

import java.time.LocalDateTime;

/**
 * 审计信息值对象
 * 
 * @author SaltyFish
 */
@Value
public class AuditInfo {
    
    Long createdBy;
    LocalDateTime createdAt;
    Long updatedBy;
    LocalDateTime updatedAt;
    
    public AuditInfo(Long createdBy, LocalDateTime createdAt, Long updatedBy, LocalDateTime updatedAt) {
        this.createdBy = createdBy;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        this.updatedBy = updatedBy;
        this.updatedAt = updatedAt != null ? updatedAt : LocalDateTime.now();
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
        return new AuditInfo(this.createdBy, this.createdAt, userId, LocalDateTime.now());
    }
}