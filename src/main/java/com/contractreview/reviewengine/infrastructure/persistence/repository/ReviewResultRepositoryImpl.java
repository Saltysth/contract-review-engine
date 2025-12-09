package com.contractreview.reviewengine.infrastructure.persistence.repository;

import com.contractreview.reviewengine.domain.enums.RiskLevel;
import com.contractreview.reviewengine.domain.model.ReviewResult;
import com.contractreview.reviewengine.domain.model.ReviewResultId;
import com.contractreview.reviewengine.domain.model.TaskId;
import com.contractreview.reviewengine.domain.repository.ReviewResultRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 审查结果仓储实现
 *
 * @author SaltyFish
 */
@Repository
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ReviewResultRepositoryImpl implements ReviewResultRepository {

    private final ReviewResultJpaRepository jpaRepository;

    @Override
    public ReviewResult save(ReviewResult reviewResult) {
        if (reviewResult == null) {
            throw new IllegalArgumentException("ReviewResult cannot be null");
        }

        ReviewResult savedResult = jpaRepository.save(reviewResult);
        log.debug("Saved review result: {}", savedResult.getId());
        return savedResult;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ReviewResult> findById(ReviewResultId id) {
        if (id == null) {
            return Optional.empty();
        }

        return jpaRepository.findById(id.getValue());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResult> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResult> findAllById(Iterable<ReviewResultId> ids) {
        if (ids == null) {
            return List.of();
        }

        List<Long> idValues = new java.util.ArrayList<>();
        for (ReviewResultId id : ids) {
            if (id != null) {
                idValues.add(id.getValue());
            }
        }

        if (idValues.isEmpty()) {
            return List.of();
        }

        return jpaRepository.findAllById(idValues);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(ReviewResultId id) {
        if (id == null) {
            return false;
        }

        return jpaRepository.existsById(id.getValue());
    }

    @Override
    @Transactional(readOnly = true)
    public long count() {
        return jpaRepository.count();
    }

    @Override
    public void deleteById(ReviewResultId id) {
        if (id == null) {
            throw new IllegalArgumentException("ReviewResultId cannot be null");
        }

        if (!existsById(id)) {
            throw new IllegalArgumentException("ReviewResult not found with id: " + id);
        }

        jpaRepository.deleteById(id.getValue());
    }

    @Override
    public void delete(ReviewResult reviewResult) {
        if (reviewResult == null) {
            throw new IllegalArgumentException("ReviewResult cannot be null");
        }

        jpaRepository.delete(reviewResult);
    }

    @Override
    public void deleteAll() {
        jpaRepository.deleteAll();
    }

    @Override
    public void deleteAllById(Iterable<ReviewResultId> ids) {
        if (ids == null) {
            return;
        }

        for (ReviewResultId id : ids) {
            if (id != null) {
                deleteById(id);
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ReviewResult> findByTaskId(TaskId taskId) {
        if (taskId == null) {
            return Optional.empty();
        }

        ReviewResult result = jpaRepository.findByTaskId(taskId.getValue());
        return Optional.ofNullable(result);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResult> findByContractId(Long contractId) {
        if (contractId == null) {
            return List.of();
        }

        return jpaRepository.findByContractId(contractId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResult> findByReviewType(String reviewType) {
        if (reviewType == null || reviewType.trim().isEmpty()) {
            return List.of();
        }

        return jpaRepository.findByReviewType(reviewType);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResult> findByOverallRiskLevel(String overallRiskLevel) {
        if (overallRiskLevel == null || overallRiskLevel.trim().isEmpty()) {
            return List.of();
        }

        return jpaRepository.findByOverallRiskLevel(overallRiskLevel);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResult> findHighRiskResults(List<RiskLevel> highRiskLevels) {
        if (highRiskLevels == null || highRiskLevels.isEmpty()) {
            return List.of();
        }

        List<String> riskLevelNames = highRiskLevels.stream()
                .map(RiskLevel::name)
                .toList();

        return jpaRepository.findHighRiskResults(riskLevelNames);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResult> findByCreatedTimeBetween(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null) {
            return List.of();
        }

        return jpaRepository.findByCreatedTimeBetween(startTime, endTime);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Object[]> countByRiskLevel() {
        return jpaRepository.countByRiskLevel();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResult> findByRiskScoreRange(Double minScore, Double maxScore) {
        // 由于ReviewResult实体中没有riskScore字段，这个方法需要根据业务需求实现
        // 可能需要根据关联的ReviewRuleResultEntity来计算
        log.warn("findByRiskScoreRange is not implemented as ReviewResult doesn't have riskScore field");
        return List.of();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewResult> findAll(Pageable pageable) {
        if (pageable == null) {
            return Page.empty();
        }

        return jpaRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewResult> findByContractId(Long contractId, Pageable pageable) {
        if (contractId == null || pageable == null) {
            return Page.empty();
        }

        return jpaRepository.findByContractId(contractId, pageable);
    }
}