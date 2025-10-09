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
    List<ContractTask> findByContractId(Long contractId);
    
    /**
     * 根据文件路径查找任务
     */
    Optional<ContractTask> findByFileUuid(String fileUUID);
    
    /**
     * 查找指定合同的最新任务
     */
    @Query("SELECT ct FROM ContractTask ct WHERE ct.contractId = :contractId ORDER BY ct.auditInfo.createdTime DESC")
    List<ContractTask> findLatestByContractId(@Param("contractId") Long contractId);
    
}