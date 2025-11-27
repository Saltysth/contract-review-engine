package com.contractreview.reviewengine.infrastructure.persistence.repository;

import com.contractreview.reviewengine.infrastructure.persistence.entity.ContractTaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 合同任务JPA仓储
 */
@Repository
public interface ContractTaskJpaRepository extends JpaRepository<ContractTaskEntity, Long> {

    /**
     * 根据合同ID查找任务
     */
    List<ContractTaskEntity> findByContractId(Long contractId);

    /**
     * 根据文件路径查找任务
     */
    Optional<ContractTaskEntity> findByFileUuid(String fileUUID);

    /**
     * 查找指定合同的最新任务
     */
    @Query("SELECT ct FROM ContractTaskEntity ct WHERE ct.contractId = :contractId ORDER BY ct.auditInfo.createdTime DESC")
    List<ContractTaskEntity> findLatestByContractId(@Param("contractId") Long contractId);

    /**
     * 根据任务ID查找合同任务
     */
    Optional<ContractTaskEntity> findByTaskId(Long taskId);

}