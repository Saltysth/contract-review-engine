package com.contractreview.reviewengine.domain.enums;

/**
 * 任务状态枚举
 * 
 * @author SaltyFish
 */
public enum TaskStatus {
    
    PENDING("待执行", "任务已创建，等待执行"),
    RUNNING("执行中", "任务正在执行"),
    COMPLETED("已完成", "任务执行成功完成"),
    FAILED("执行失败", "任务执行失败"),
    CANCELLED("已取消", "任务被取消");
    
    private final String displayName;
    private final String description;
    
    TaskStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * 检查是否可以执行
     */
    public boolean canExecute() {
        return this == PENDING;
    }
    
    /**
     * 检查是否已完成（成功或失败）
     */
    public boolean isFinished() {
        return this == COMPLETED || this == FAILED || this == CANCELLED;
    }
    
    /**
     * 检查是否可以重试
     */
    public boolean canRetry() {
        return this == FAILED;
    }
}