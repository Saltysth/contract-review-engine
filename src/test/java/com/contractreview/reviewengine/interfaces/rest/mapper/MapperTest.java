package com.contractreview.reviewengine.interfaces.rest.mapper;

import com.contractreview.reviewengine.interfaces.rest.dto.ContractReviewRequestDto;
import com.contractreview.reviewengine.domain.enums.ReviewType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * MapStruct映射器基础测试
 */
@SpringBootTest
class MapperTest {

    @Autowired
    private ContractTaskMapper contractTaskMapper;

    @Test
    void shouldMapRequestDtoToTaskConfiguration() {
        // Given
        ContractReviewRequestDto request = ContractReviewRequestDto.builder()
                .contractId("CONTRACT-2024-001")
                .filePath("/contracts/test.pdf")
                .fileHash("sha256:abc123")
                .reviewType(ReviewType.FULL_REVIEW)
                .priority(8)
                .timeoutMinutes(45)
                .maxRetries(5)
                .retryIntervalSeconds(120)
                .build();

        // When
        var taskConfiguration = contractTaskMapper.toTaskConfiguration(request);

        // Then
        assertThat(taskConfiguration).isNotNull();
        assertThat(taskConfiguration.getPriority()).isEqualTo(8);
        assertThat(taskConfiguration.getTimeout()).isEqualTo(java.time.Duration.ofMinutes(45));
        assertThat(taskConfiguration.getRetryPolicy().getMaxRetries()).isEqualTo(5);
        assertThat(taskConfiguration.getRetryPolicy().getRetryInterval())
                .isEqualTo(java.time.Duration.ofSeconds(120));
    }
}