package com.contractreview.reviewengine.interfaces.rest.mapper;

import com.contractreview.reviewengine.domain.model.ContractReview;
import com.contractreview.reviewengine.domain.valueobject.TaskConfiguration;
import com.contractreview.reviewengine.interfaces.rest.dto.ContractReviewRequestDto;
import com.contractreview.reviewengine.interfaces.rest.dto.ContractTaskDto;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

/**
 * 合同任务映射器
 */
@Mapper(config = GlobalMappingConfig.class)
public interface ContractTaskMapper {
    
    ContractTaskMapper INSTANCE = Mappers.getMapper(ContractTaskMapper.class);
    
    /**
     * Domain对象转DTO
     */
    @Mapping(target = "reviewRules",  ignore = true)
    ContractTaskDto toDto(ContractReview contractReview);

    /**
     * 请求DTO转Domain对象（用于创建）
     */
    ContractReview toEntity(ContractReviewRequestDto dto);
    
    /**
     * 请求DTO转任务配置
     */
    @Mapping(target = "retryPolicy", source = ".", qualifiedByName = "buildRetryPolicy")
    @Mapping(target = "timeoutSeconds", source = "timeoutMinutes", qualifiedByName = "minutesToSeconds")
    @Mapping(target = "priority", source = "priority")
    @Mapping(target = "customSettings", ignore = true)
    TaskConfiguration toTaskConfiguration(ContractReviewRequestDto dto);
    
    /**
     * TaskId转字符串 - ContractTask专用
     */
    @Named("contractTaskIdToString")
    default String contractTaskIdToString(Long taskId) {
        return taskId != null ? taskId.toString() : null;
    }
    
    /**
     * 秒转毫秒
     */
    @Named("secondsToMilliseconds")
    default Long secondsToMilliseconds(Integer seconds) {
        return seconds != null ? seconds * 1000L : null;
    }
    
    /**
     * 分钟转秒
     */
    @Named("minutesToSeconds")
    default Integer minutesToSeconds(Integer minutes) {
        return minutes != null ? minutes * 60 : null;
    }
    
    /**
     * 构建重试策略
     */
    @Named("buildRetryPolicy")
    default com.contractreview.reviewengine.domain.valueobject.RetryPolicy buildRetryPolicy(
        ContractReviewRequestDto dto) {
        if (dto == null) {
            return com.contractreview.reviewengine.domain.valueobject.RetryPolicy.defaultPolicy();
        }

        int maxRetries = dto.getMaxRetries() != null ? dto.getMaxRetries() : 3;
        long initialDelayMs = dto.getRetryIntervalSeconds() != null ? dto.getRetryIntervalSeconds() * 1000L : 1000L;

        return com.contractreview.reviewengine.domain.valueobject.RetryPolicy.builder()
                .maxRetries(maxRetries)
                .maxRetryIntervalMs(Math.max(initialDelayMs * 10, 30000L))
                .backoffMultiplier(2.0)
                .exponentialBackoff(true)
                .build();
    }
}