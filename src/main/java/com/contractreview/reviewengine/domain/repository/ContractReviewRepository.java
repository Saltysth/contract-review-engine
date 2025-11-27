package com.contractreview.reviewengine.domain.repository;

import com.contractreview.reviewengine.domain.model.ContractReview;
import com.contractreview.reviewengine.domain.model.TaskId;

import java.util.List;
import java.util.Optional;

/**
 * 合同审查领域仓储接口
 */
public interface ContractReviewRepository {

    /**
     * 保存合同审查
     */
    ContractReview save(ContractReview contractReview);

    /**
     * 根据任务ID查找
     */
    Optional<ContractReview> findById(TaskId taskId);

    /**
     * 根据合同ID查找任务列表
     */
    List<ContractReview> findByContractId(Long contractId);

    /**
     * 根据文件UUID查找
     */
    Optional<ContractReview> findByFileUuid(String fileUuid);

    /**
     * 查找指定合同的最新任务
     */
    List<ContractReview> findLatestByContractId(Long contractId);

    /**
     * 根据任务ID查找合同任务实体（infra层专用）
     */
    Optional<ContractReview> findByTaskId(TaskId taskId);

    /**
     * 删除合同审查
     */
    void delete(TaskId taskId);

}