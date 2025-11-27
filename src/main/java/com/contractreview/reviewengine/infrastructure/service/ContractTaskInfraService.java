package com.contractreview.reviewengine.infrastructure.service;

import com.contractreview.exception.core.BusinessException;
import com.contractreview.exception.enums.CommonErrorCode;
import com.contractreview.reviewengine.domain.model.ContractReview;
import com.contractreview.reviewengine.domain.model.TaskId;
import com.contractreview.reviewengine.domain.repository.ContractReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * 合同任务基础设施服务
 * 专门处理一对一关系的合同任务查询和业务逻辑
 *
 * @author SaltyFish
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ContractTaskInfraService {

    private final ContractReviewRepository contractReviewRepository;

    /**
     * 根据任务ID查询对应的合同任务
     *
     * 由于task_id和contract_task是一一对应的关系，该方法会对结果进行严格校验
     * 如果task存在但对应的contract_task不存在，抛出业务异常
     *
     * @param taskId 任务ID
     * @return 对应的合同任务
     * @throws BusinessException 当task存在但contract_task不存在时抛出
     */
    public ContractReview findContractTaskByTaskId(TaskId taskId) {
        if (taskId == null) {
            throw new BusinessException(CommonErrorCode.PARAM_INVALID, "任务ID不能为空");
        }

        log.debug("查询合同任务，taskId: {}", taskId.getValue());

        Optional<ContractReview> contractTaskOpt = contractReviewRepository.findByTaskId(taskId);

        if (contractTaskOpt.isEmpty()) {
            log.warn("未找到对应的合同任务，taskId: {}", taskId.getValue());
            throw new BusinessException(
                CommonErrorCode.DATA_NOT_FOUND,
                "任务ID [{0}] 对应的合同任务不存在，请确认任务类型和状态",
                taskId.getValue()
            );
        }

        ContractReview contractTask = contractTaskOpt.get();
        log.debug("成功找到合同任务，taskId: {}, contractId: {}", taskId.getValue(), contractTask.getContractId());

        return contractTask;
    }

    /**
     * 根据任务ID查询对应的合同任务（可选返回）
     *
     * 与findContractTaskByTaskId不同，此方法不会抛出异常，而是返回Optional
     * 用于需要区分"task不存在"和"contract_task不存在"的场景
     *
     * @param taskId 任务ID
     * @return 合同任务的Optional包装
     */
    public Optional<ContractReview> findContractTaskByTaskIdOptional(TaskId taskId) {
        if (taskId == null) {
            return Optional.empty();
        }

        log.debug("查询合同任务（可选返回），taskId: {}", taskId.getValue());

        return contractReviewRepository.findByTaskId(taskId);
    }

    /**
     * 验证任务ID是否有对应的合同任务
     *
     * @param taskId 任务ID
     * @return true如果存在对应的合同任务，false如果不存在
     */
    public boolean hasContractTask(TaskId taskId) {
        if (taskId == null) {
            return false;
        }

        return contractReviewRepository.findByTaskId(taskId).isPresent();
    }
}