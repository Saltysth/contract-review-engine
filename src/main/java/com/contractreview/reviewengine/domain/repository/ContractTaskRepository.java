package com.contractreview.reviewengine.domain.repository;

import com.contractreview.reviewengine.domain.model.ContractTask;
import com.contractreview.reviewengine.domain.model.TaskId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 合同任务仓储接口
 */
@Repository
public interface ContractTaskRepository extends JpaRepository<ContractTask, TaskId> {
    
    /**
     * 根据合同ID查找任务
     */
    List<ContractTask> findByContractId(String contractId);
    
    /**
     * 根据文件路径查找任务
     */
    Optional<ContractTask> findByFilePath(String filePath);
    
    /**
     * 查找指定合同的最新任务
     */
    @Query("SELECT ct FROM ContractTask ct WHERE ct.contractId = :contractId ORDER BY ct.auditInfo.createdAt DESC")
    List<ContractTask> findLatestByContractId(@Param("contractId") String contractId);
    
    /**
     * 根据文件哈希查找任务（避免重复处理相同文件）
     */
    Optional<ContractTask> findByFileHash(String fileHash);
    
    /**
     * 查找包含特定元数据的任务
     */
    @Query("SELECT ct FROM ContractTask ct WHERE JSON_EXTRACT(ct.metadata, '$.department') = :department")
    List<ContractTask> findByDepartment(@Param("department") String department);
}