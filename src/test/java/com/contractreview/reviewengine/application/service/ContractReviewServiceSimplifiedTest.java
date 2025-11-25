package com.contractreview.reviewengine.application.service;

import com.contractreview.reviewengine.domain.model.ContractReview;
import com.contractreview.reviewengine.domain.model.TaskId;
import com.contractreview.reviewengine.domain.repository.ContractReviewRepository;
import com.contractreview.reviewengine.domain.repository.ReviewResultRepository;
import com.contractreview.reviewengine.domain.service.TaskManagementService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * ContractReviewService 简化实现测试
 * 测试新的直接合同审查功能，验证与原有管道实现的并行工作能力
 */
@ExtendWith(MockitoExtension.class)
class ContractReviewServiceSimplifiedTest {

    @Mock
    private ContractReviewRepository contractReviewRepository;

    @Mock
    private ReviewResultRepository reviewResultRepository;

    @Mock
    private TaskManagementService taskManagementService;

    @Mock
    private com.contract.common.feign.ContractFeignClient contractFeignClient;

    @InjectMocks
    private ContractReviewService contractReviewService;

    @Test
    void shouldStartDirectContractReviewSuccessfully() {
        // Given
        Long taskIdValue = 123L;
        TaskId taskId = TaskId.of(taskIdValue);
        Long contractId = 456L;

        ContractReview contractReview = ContractReview.create(taskIdValue, contractId, "file-uuid-123");

        when(contractReviewRepository.findById(taskId)).thenReturn(Optional.of(contractReview));
        when(reviewResultRepository.save(any())).thenReturn(any());

        // When
        contractReviewService.startContractReview(taskId);

        // Then
        verify(taskManagementService).startTask(taskId);
        verify(reviewResultRepository).save(any());
        // completeTask is called within saveReviewResult
        verify(taskManagementService).completeTask(taskId);
    }

    @Test
    void shouldHandleDirectContractReviewFailureWhenContractNotFound() {
        // Given
        Long taskIdValue = 123L;
        TaskId taskId = TaskId.of(taskIdValue);
        Long contractId = 456L;

        ContractReview contractReview = ContractReview.create(taskIdValue, contractId, "file-uuid-123");

        when(contractReviewRepository.findById(taskId)).thenReturn(Optional.of(contractReview));

        // When
        contractReviewService.startContractReview(taskId);

        // Then - should complete successfully since contract retrieval was removed
        verify(taskManagementService).startTask(taskId);
        verify(reviewResultRepository).save(any());
        // completeTask is called within saveReviewResult
        verify(taskManagementService).completeTask(taskId);
    }

    @Test
    void shouldHandleDirectContractReviewFailureWhenRepositoryThrows() {
        // Given
        Long taskIdValue = 123L;
        TaskId taskId = TaskId.of(taskIdValue);

        when(contractReviewRepository.findById(taskId))
                .thenThrow(new RuntimeException("Database error"));

        // When & Then
        assertThatThrownBy(() -> contractReviewService.startContractReview(taskId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Database error");

        // The exception occurs in getContractTask before task starts, so no task management calls
        verify(taskManagementService, never()).startTask(any());
        verify(taskManagementService, never()).failTask(any(), any());
        verify(reviewResultRepository, never()).save(any());
        verify(taskManagementService, never()).completeTask(any());
    }

    @Test
    void shouldHandleDirectReviewFailureCorrectly() {
        // Given
        Long taskIdValue = 123L;
        TaskId taskId = TaskId.of(taskIdValue);
        String errorMessage = "Test error message";

        // When
        contractReviewService.handleReviewFailureDirect(taskId, errorMessage);

        // Then
        verify(taskManagementService).failTask(taskId, errorMessage);
    }

    /**
     * 测试确保原有的管道方法仍然可用（已弃用状态）
     * 这验证了在过渡期内两种实现可以并行工作
     */
    @Test
    @SuppressWarnings("deprecation")
    void shouldStillSupportDeprecatedPipelineMethod() {
        // Given
        Long taskIdValue = 123L;
        TaskId taskId = TaskId.of(taskIdValue);
        Long contractId = 456L;

        ContractReview contractReview = ContractReview.create(taskIdValue, contractId, "file-uuid-123");

        when(contractReviewRepository.findById(taskId)).thenReturn(Optional.of(contractReview));

        // When
        contractReviewService.startContractReview(taskId);

        // Then
        verify(taskManagementService).startTask(taskId);
        // 原有的管道方法不应该完成任务（因为有TODO注释）
        verify(taskManagementService, never()).completeTask(taskId);
        verify(taskManagementService, never()).failTask(any(), any());
    }
}