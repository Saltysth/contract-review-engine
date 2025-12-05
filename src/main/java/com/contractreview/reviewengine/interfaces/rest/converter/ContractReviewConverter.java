package com.contractreview.reviewengine.interfaces.rest.converter;

import com.contractreview.reviewengine.domain.valueobject.ContractMetadata;
import com.contractreview.reviewengine.domain.valueobject.RetryPolicy;
import com.contractreview.reviewengine.domain.valueobject.ReviewConfiguration;
import com.contractreview.reviewengine.domain.valueobject.TaskConfiguration;
import com.contractreview.reviewengine.interfaces.rest.dto.ContractReviewRequestDto;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 合同审查请求转换器
 *
 * @author SaltyFish
 */
@Component
public class ContractReviewConverter {

    /**
     * 转换DTO为任务配置
     */
    public TaskConfiguration convertToTaskConfiguration(ContractReviewRequestDto dto) {
        if (dto == null) {
            return TaskConfiguration.defaultTaskConfiguration();
        }

        // 构建重试策略
        RetryPolicy retryPolicy = RetryPolicy.builder()
                .maxRetries(dto.getMaxRetries() != null ? dto.getMaxRetries() : 3)
                .retryIntervalMs(dto.getRetryIntervalSeconds() != null ?
                    dto.getRetryIntervalSeconds() * 1000L : 60000L)
                .retryCount(0)
                .build();

        // 构建自定义设置
        Map<String, Object> customSettings = new HashMap<>();
        customSettings.put("contractTitle", dto.getContractTitle());

        // 构建任务配置
        return TaskConfiguration.builder()
                .retryPolicy(retryPolicy)
                .timeoutSeconds(dto.getTimeoutMinutes() != null ?
                    dto.getTimeoutMinutes() * 60 : 1800)
                .priority(dto.getPriority() != null ? dto.getPriority() : 5)
                .customSettings(customSettings)
                .build();
    }

    /**
     * 转换DTO为合同元数据
     */
    public ContractMetadata convertToContractMetadata(ContractReviewRequestDto dto) {
        if (dto == null) {
            return null;
        }

        return ContractMetadata.builder()
                .contractId(dto.getContractId())
                .fileUuid(dto.getFileUuid())
                .businessTags(dto.getBusinessTags())
                .build();
    }

    /**
     * 转换DTO为审查配置
     */
    public ReviewConfiguration convertToReviewConfiguration(ContractReviewRequestDto dto) {
        if (dto == null) {
            return ReviewConfiguration.defaultConfiguration();
        }

        // 转换合同类型枚举为字符串
        String contractTypeStr = null;
        if (dto.getContractType() != null) {
            contractTypeStr = dto.getContractType().name();
        }

        return ReviewConfiguration.builder()
                .reviewType(dto.getReviewType())
                .customSelectedReviewTypes(dto.getCustomSelectedReviewTypes())
                .industry(dto.getIndustry())
                .currency(dto.getCurrency())
                .contractType(contractTypeStr)
                .reviewRules(dto.getReviewRules())
                .promptTemplate(dto.getPromptTemplate())
                .enableTerminology(dto.getEnableTerminology() != null ?
                    dto.getEnableTerminology() : true)
                .build();
    }

    /**
     * 检查任务配置是否需要更新
     */
    public boolean needsTaskConfigurationUpdate(ContractReviewRequestDto dto,
                                              TaskConfiguration currentConfig) {
        if (dto == null || currentConfig == null) {
            return false;
        }

        // 检查优先级
        if (dto.getPriority() != null &&
            !dto.getPriority().equals(currentConfig.getPriority())) {
            return true;
        }

        // 检查超时时间
        Integer newTimeoutSeconds = dto.getTimeoutMinutes() != null ?
            dto.getTimeoutMinutes() * 60 : null;
        if (newTimeoutSeconds != null &&
            !newTimeoutSeconds.equals(currentConfig.getTimeoutSeconds())) {
            return true;
        }

        // 检查重试策略
        if (dto.getMaxRetries() != null || dto.getRetryIntervalSeconds() != null) {
            RetryPolicy currentRetryPolicy = currentConfig.getRetryPolicy();
            if (currentRetryPolicy == null) {
                return true;
            }

            if (dto.getMaxRetries() != null &&
                !dto.getMaxRetries().equals(currentRetryPolicy.getMaxRetries())) {
                return true;
            }

            if (dto.getRetryIntervalSeconds() != null) {
                Long newIntervalMs = dto.getRetryIntervalSeconds() * 1000L;
                if (!newIntervalMs.equals(currentRetryPolicy.getRetryIntervalMs())) {
                    return true;
                }
            }
        }

        // 检查自定义设置中的合同标题
        if (dto.getContractTitle() != null) {
            Object currentTitle = currentConfig.getCustomSetting("contractTitle");
            if (currentTitle == null || !dto.getContractTitle().equals(currentTitle)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 检查合同元数据是否需要更新
     */
    public boolean needsContractMetadataUpdate(ContractReviewRequestDto dto,
                                             ContractMetadata currentMetadata) {
        if (dto == null || currentMetadata == null) {
            return false;
        }

        // 检查业务标签
        List<String> newTags = dto.getBusinessTags();
        if (newTags != null && !newTags.equals(currentMetadata.getBusinessTags())) {
            return true;
        }

        return false;
    }

    /**
     * 检查审查配置是否需要更新
     */
    public boolean needsReviewConfigurationUpdate(ContractReviewRequestDto dto,
                                                ReviewConfiguration currentConfig) {
        if (dto == null || currentConfig == null) {
            return false;
        }

        // 检查审查类型
        if (dto.getReviewType() != null &&
            !dto.getReviewType().equals(currentConfig.getReviewType())) {
            return true;
        }

        // 检查合同类型
        if (dto.getContractType() != null) {
            String newContractType = dto.getContractType().name();
            if (!newContractType.equals(currentConfig.getContractType())) {
                return true;
            }
        }

        // 检查合同状态 不能更改正式配置
        if (dto.isDraft() && !currentConfig.isDraft()) {
            return false;
        }

        // 检查提示模板
        if (dto.getPromptTemplate() != null &&
            !dto.getPromptTemplate().equals(currentConfig.getPromptTemplate())) {
            return true;
        }

        // 检查是否启用术语库
        if (dto.getEnableTerminology() != null &&
            !dto.getEnableTerminology().equals(currentConfig.getEnableTerminology())) {
            return true;
        }

        // 检查自定义选择审查项
        if (dto.getCustomSelectedReviewTypes() != null &&
            !dto.getCustomSelectedReviewTypes().equals(currentConfig.getCustomSelectedReviewTypes())) {
            return true;
        }

        // 检查审查规则
        if (dto.getReviewRules() != null &&
            !dto.getReviewRules().equals(currentConfig.getReviewRules())) {
            return true;
        }

        // 检查行业
        if (dto.getIndustry() != null &&
            !dto.getIndustry().equals(currentConfig.getIndustry())) {
            return true;
        }

        // 检查币种
        if (dto.getCurrency() != null &&
            !dto.getCurrency().equals(currentConfig.getCurrency())) {
            return true;
        }

        return false;
    }
}