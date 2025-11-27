package com.contractreview.reviewengine.infrastructure.persistence.repository;

import com.contractreview.reviewengine.domain.model.ContractReview;
import com.contractreview.reviewengine.domain.model.TaskId;
import com.contractreview.reviewengine.domain.repository.ContractReviewRepository;
import com.contractreview.reviewengine.infrastructure.persistence.converter.ContractReviewDomainConverter;
import com.contractreview.reviewengine.infrastructure.persistence.entity.ContractTaskEntity;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 合同审查仓储实现
 */
@Repository
@RequiredArgsConstructor
public class ContractReviewRepositoryImpl implements ContractReviewRepository {

    private final ContractTaskJpaRepository jpaRepository;
    private final ContractReviewDomainConverter converter;

    @Override
    public ContractReview save(ContractReview contractReview) {
        ContractTaskEntity entity = converter.toEntity(contractReview);
        ContractTaskEntity savedEntity = jpaRepository.save(entity);
        return converter.toDomain(savedEntity);
    }

    @Override
    public Optional<ContractReview> findById(TaskId taskId) {
        return jpaRepository.findById(taskId.getValue())
                .map(converter::toDomain);
    }

    @Override
    public List<ContractReview> findByContractId(Long contractId) {
        return jpaRepository.findByContractId(contractId).stream()
                .map(converter::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<ContractReview> findByFileUuid(String fileUuid) {
        return jpaRepository.findByFileUuid(fileUuid)
                .map(converter::toDomain);
    }

    @Override
    public List<ContractReview> findLatestByContractId(Long contractId) {
        return jpaRepository.findLatestByContractId(contractId).stream()
                .map(converter::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(@NotBlank TaskId taskId) {
        jpaRepository.deleteById(taskId.getValue());
    }

    @Override
    public Optional<ContractReview> findByTaskId(TaskId taskId) {
        return jpaRepository.findByTaskId(taskId.getValue())
                .map(converter::toDomain);
    }

}