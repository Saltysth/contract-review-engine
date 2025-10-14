package com.contractreview.reviewengine.domain.repository;

import com.contractreview.reviewengine.domain.model.ContractReview;
import com.contractreview.reviewengine.domain.model.TaskId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 合同任务仓储接口（已废弃，请使用ContractReviewRepository）
 * @deprecated 请使用 {@link ContractReviewRepository}
 */
@Deprecated(forRemoval = true)
@Repository
public interface ContractTaskRepository extends JpaRepository<ContractReview, TaskId> {

    /**
     * 根据合同ID查找任务
     * @deprecated 请使用 {@link ContractReviewRepository#findByContractId(Long)}
     */
    @Deprecated(forRemoval = true)
    List<ContractReview> findByContractId(Long contractId);

    /**
     * 根据文件路径查找任务
     * @deprecated 请使用 {@link ContractReviewRepository#findByFileUuid(String)}
     */
    @Deprecated(forRemoval = true)
    Optional<ContractReview> findByFileUuid(String fileUUID);

    /**
     * 查找指定合同的最新任务
     * @deprecated 请使用 {@link ContractReviewRepository#findLatestByContractId(Long)}
     */
    @Deprecated(forRemoval = true)
    @Query("SELECT cr FROM ContractReview cr WHERE cr.contractMetadata.contractId = :contractId ORDER BY cr.auditInfo.createdTime DESC")
    List<ContractReview> findLatestByContractId(@Param("contractId") Long contractId);

}