package com.contractreview.reviewengine.infrastructure.persistence.repository;

import com.contractreview.reviewengine.domain.enums.ReviewType;
import com.contractreview.reviewengine.infrastructure.persistence.entity.ContractTaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * ContractTask实体JPA Repository
 *
 * @author SaltyFish
 */
@Repository
public interface ContractTaskEntityRepository extends JpaRepository<ContractTaskEntity, Long> {

    /**
     * 根据taskId查找合同任务
     */
    ContractTaskEntity findByTaskId(Long taskId);

    /**
     * 统计合同任务总数（带过滤条件）
     */
    @Query(value = """
        SELECT COUNT(ct.id)
        FROM contract_task ct
        WHERE (:contractId IS NULL OR ct.contract_id = :contractId)
          AND (:reviewType IS NULL OR ct.review_type = :reviewType)
          AND (:businessTag IS NULL OR :businessTag = ANY(string_to_array(ct.business_tags, ',')))
        """, nativeQuery = true)
    Long countByConditions(
        @Param("contractId") Long contractId,
        @Param("reviewType") String reviewType,
        @Param("businessTag") String businessTag
    );

    /**
     * 根据合同ID查找任务
     */
    @Query("SELECT ct FROM ContractTaskEntity ct WHERE ct.contractId = :contractId ORDER BY ct.auditInfo.createdTime DESC")
    List<ContractTaskEntity> findByContractIdOrderByCreatedTimeDesc(@Param("contractId") Long contractId);

    /**
     * 根据审查类型查找任务
     */
    @Query("SELECT ct FROM ContractTaskEntity ct WHERE ct.reviewType = :reviewType ORDER BY ct.auditInfo.createdTime DESC")
    List<ContractTaskEntity> findByReviewTypeOrderByCreatedTimeDesc(@Param("reviewType") ReviewType reviewType);
}