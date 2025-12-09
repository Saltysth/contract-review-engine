package com.contractreview.reviewengine.infrastructure.persistence.repository;

import com.contractreview.reviewengine.domain.enums.RiskLevel;
import com.contractreview.reviewengine.domain.model.ReviewRuleResultEntity;
import com.contractreview.reviewengine.domain.model.ReviewRuleResultId;
import com.contractreview.reviewengine.domain.model.ReviewResultId;
import com.contractreview.reviewengine.domain.repository.ReviewRuleResultRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 审查规则结果仓储实现
 *
 * @author SaltyFish
 */
@Repository
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ReviewRuleResultRepositoryImpl implements ReviewRuleResultRepository {

    private final ReviewRuleResultJpaRepository jpaRepository;

    @Override
    public ReviewRuleResultEntity save(ReviewRuleResultEntity reviewRuleResult) {
        if (reviewRuleResult == null) {
            throw new IllegalArgumentException("ReviewRuleResultEntity cannot be null");
        }

        ReviewRuleResultEntity savedResult = jpaRepository.save(reviewRuleResult);
        log.debug("Saved review rule result: {}", savedResult.getId());
        return savedResult;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ReviewRuleResultEntity> findById(ReviewRuleResultId id) {
        if (id == null) {
            return Optional.empty();
        }

        return jpaRepository.findById(id.getValue());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewRuleResultEntity> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewRuleResultEntity> findAllById(Iterable<ReviewRuleResultId> ids) {
        if (ids == null) {
            return List.of();
        }

        List<Long> idValues = new java.util.ArrayList<>();
        for (ReviewRuleResultId id : ids) {
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
    public boolean existsById(ReviewRuleResultId id) {
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
    public void deleteById(ReviewRuleResultId id) {
        if (id == null) {
            throw new IllegalArgumentException("ReviewRuleResultId cannot be null");
        }

        if (!existsById(id)) {
            throw new IllegalArgumentException("ReviewRuleResult not found with id: " + id);
        }

        jpaRepository.deleteById(id.getValue());
    }

    @Override
    public void delete(ReviewRuleResultEntity reviewRuleResult) {
        if (reviewRuleResult == null) {
            throw new IllegalArgumentException("ReviewRuleResultEntity cannot be null");
        }

        jpaRepository.delete(reviewRuleResult);
    }

    @Override
    public void deleteAll() {
        jpaRepository.deleteAll();
    }

    @Override
    public void deleteAllById(Iterable<ReviewRuleResultId> ids) {
        if (ids == null) {
            return;
        }

        for (ReviewRuleResultId id : ids) {
            if (id != null) {
                deleteById(id);
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewRuleResultEntity> findByReviewResultId(ReviewResultId reviewResultId) {
        if (reviewResultId == null) {
            return List.of();
        }

        return jpaRepository.findByReviewResultId(reviewResultId.getValue());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewRuleResultEntity> findByRiskName(String riskName) {
        if (riskName == null || riskName.trim().isEmpty()) {
            return List.of();
        }

        return jpaRepository.findByRiskName(riskName);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewRuleResultEntity> findByRuleType(String ruleType) {
        if (ruleType == null || ruleType.trim().isEmpty()) {
            return List.of();
        }

        return jpaRepository.findByRuleType(ruleType);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewRuleResultEntity> findByRiskLevel(RiskLevel riskLevel) {
        if (riskLevel == null) {
            return List.of();
        }

        return jpaRepository.findByRiskLevel(riskLevel);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewRuleResultEntity> findByRiskScoreBetween(Double minScore, Double maxScore) {
        if (minScore == null || maxScore == null || minScore > maxScore) {
            return List.of();
        }

        return jpaRepository.findByRiskScoreBetween(minScore, maxScore);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewRuleResultEntity> findHighRiskResults(List<RiskLevel> highRiskLevels) {
        if (highRiskLevels == null || highRiskLevels.isEmpty()) {
            return List.of();
        }

        return jpaRepository.findHighRiskResults(highRiskLevels);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ReviewRuleResultEntity> findByRiskClauseId(String riskClauseId) {
        if (riskClauseId == null || riskClauseId.trim().isEmpty()) {
            return Optional.empty();
        }

        ReviewRuleResultEntity result = jpaRepository.findByRiskClauseId(riskClauseId);
        return Optional.ofNullable(result);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewRuleResultEntity> findByOriginContractTextContaining(String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            return List.of();
        }

        return jpaRepository.findByOriginContractTextContaining(searchText);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewRuleResultEntity> findByRuleTypeAndRiskLevel(String ruleType, RiskLevel riskLevel) {
        if (ruleType == null || ruleType.trim().isEmpty() || riskLevel == null) {
            return List.of();
        }

        return jpaRepository.findByRuleTypeAndRiskLevel(ruleType, riskLevel);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Object[]> countByRiskLevel() {
        return jpaRepository.countByRiskLevel();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Object[]> countByRuleType() {
        return jpaRepository.countByRuleType();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewRuleResultEntity> findAll(Pageable pageable) {
        if (pageable == null) {
            return Page.empty();
        }

        return jpaRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewRuleResultEntity> findByReviewResultId(ReviewResultId reviewResultId, Pageable pageable) {
        if (reviewResultId == null || pageable == null) {
            return Page.empty();
        }

        List<ReviewRuleResultEntity> results = jpaRepository.findByReviewResultId(reviewResultId.getValue());
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), results.size());

        if (start >= results.size()) {
            return new PageImpl<>(List.of(), pageable, results.size());
        }

        return new PageImpl<>(results.subList(start, end), pageable, results.size());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewRuleResultEntity> findByContractId(Long contractId) {
        if (contractId == null) {
            return List.of();
        }

        return jpaRepository.findByContractId(contractId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewRuleResultEntity> findByTaskId(Long taskId) {
        if (taskId == null) {
            return List.of();
        }

        return jpaRepository.findByTaskId(taskId);
    }

    @Override
    public List<ReviewRuleResultEntity> saveAll(List<ReviewRuleResultEntity> reviewRuleResults) {
        if (reviewRuleResults == null || reviewRuleResults.isEmpty()) {
            return List.of();
        }

        List<ReviewRuleResultEntity> savedResults = jpaRepository.saveAll(reviewRuleResults);
        log.debug("Batch saved {} review rule results", savedResults.size());
        return savedResults;
    }

    @Override
    public void deleteAllByReviewResultId(ReviewResultId reviewResultId) {
        if (reviewResultId == null) {
            throw new IllegalArgumentException("ReviewResultId cannot be null");
        }

        jpaRepository.deleteAllByReviewResultId(reviewResultId.getValue());
        log.debug("Deleted all rule results for review result: {}", reviewResultId);
    }
}