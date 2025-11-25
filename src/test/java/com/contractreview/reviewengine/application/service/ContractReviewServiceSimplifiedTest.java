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
    void shouldStartContractReviewSuccessfully() {
        // Given
        Long taskIdValue = 123L;
        TaskId taskId = TaskId.of(taskIdValue);
        Long contractId = 456L;

        ContractReview contractReview = ContractReview.create(taskIdValue, contractId, "file-uuid-123");
        com.contractreview.reviewengine.domain.model.Task task =
            com.contractreview.reviewengine.domain.model.Task.create("Test Task",
                com.contractreview.reviewengine.domain.enums.TaskType.CLASSIFICATION, 1L);

        when(contractReviewRepository.findById(taskId)).thenReturn(Optional.of(contractReview));
        // Mock taskRepository behavior for the new implementation
        when(taskManagementService.getTaskStatus(taskId)).thenReturn(com.contractreview.reviewengine.domain.enums.TaskStatus.PENDING);

        // When & Then - New implementation should not throw exception
        // The task is prepared for processing by the scheduler
        contractReviewService.startContractReview(taskId);

        // In the new implementation, startContractReview only validates the task state
        // The actual processing is done by the scheduler
    }

    @Test
    void shouldThrowExceptionWhenTaskNotFound() {
        // Given
        Long taskIdValue = 123L;
        TaskId taskId = TaskId.of(taskIdValue);

        when(contractReviewRepository.findById(taskId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> contractReviewService.startContractReview(taskId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("任务不存在");
    }

    @Test
    void shouldHandleReviewFailureCorrectly() {
        // Given
        Long taskIdValue = 123L;
        TaskId taskId = TaskId.of(taskIdValue);
        String errorMessage = "Test error message";

        // When
        contractReviewService.handleReviewFailure(taskId, errorMessage);

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