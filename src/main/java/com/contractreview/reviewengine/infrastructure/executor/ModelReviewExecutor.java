package com.contractreview.reviewengine.infrastructure.executor;

import com.alibaba.nacos.shaded.com.google.common.collect.Lists;
import com.contract.ai.feign.client.AiClient;
import com.contract.ai.feign.dto.ChatRequest;
import com.contract.ai.feign.enums.ModelType;
import com.contract.ai.feign.enums.PlatFormType;
import com.contract.common.feign.ClauseFeignClient;
import com.contract.common.feign.PromptFeignClient;
import com.contract.common.feign.ReviewRuleFeignClient;
import com.contract.common.feign.dto.ClauseFeignDTO;
import com.contract.common.feign.dto.PromptFeignDTO;
import com.contract.common.feign.dto.PromptPageResultFeignDTO;
import com.contract.common.feign.dto.PromptQueryFeignDTO;
import com.contract.common.feign.dto.ReviewRuleFeignDTO;
import com.contract.common.feign.dto.ReviewRulePageResultFeignDTO;
import com.contract.common.feign.dto.ReviewRuleQueryFeignDTO;
import com.contractreview.reviewengine.application.service.ContractReviewService;
import com.contractreview.reviewengine.domain.enums.ExecutionStage;
import com.contractreview.reviewengine.domain.enums.RiskLevel;
import com.contractreview.reviewengine.domain.enums.TaskStatus;
import com.contractreview.reviewengine.domain.model.ContractReview;
import com.contractreview.reviewengine.domain.model.ReviewResult;
import com.contractreview.reviewengine.domain.model.ReviewType;
import com.contractreview.reviewengine.domain.model.Task;
import com.contractreview.reviewengine.domain.repository.TaskRepository;
import com.contractreview.reviewengine.domain.valueobject.ReviewConfiguration;
import com.contractreview.reviewengine.domain.valueobject.ReviewRuleResult;
import com.contractreview.reviewengine.domain.valueobject.TaskConfiguration;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;
import java.util.Map;

/**
 * 模型审查执行器
 * 负责处理AI模型审查阶段的任务
 *
 * @author SaltyFish
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ModelReviewExecutor {

    private final TaskRepository taskRepository;
    private final ContractReviewService contractReviewService;
    private final ReviewRuleFeignClient reviewRuleFeignClient;
    private final ClauseFeignClient clauseFeignClient;
    private final PromptFeignClient promptFeignClient;
    private final AiClient aiClient;
    private final ObjectMapper objectMapper;

    /**
     * 批量处理模型审查任务
     */
    @Transactional
    public void processBatch(List<Task> tasks) {
        if (tasks.isEmpty()) {
            return;
        }

        log.info("开始处理 {} 个模型审查任务", tasks.size());

        int successCount = 0;
        int failureCount = 0;

        for (Task task : tasks) {
            try {
                log.info("准备模型审查");
                processSingleTask(task);
                successCount++;
                log.debug("任务 {} 模型审查处理成功", task.getId());

            } catch (Exception e) {
                handleTaskExecutionFailure(task, e);
                failureCount++;
                log.error("任务 {} 模型审查处理失败: {}", task.getId(), e.getMessage(), e);
            }
        }

        log.info("模型审查批次处理完成，成功 {} 个，失败 {} 个", successCount, failureCount);
    }

    /**
     * 处理单个模型审查任务
     */
    private void processSingleTask(Task task) {
        if (!task.getStatus().equals(TaskStatus.PENDING)) {
            log.info("只执行待处理任务，当前任务：{}, 状态： {}", task.getId(), task.getStatus());
            return;
        }

        ContractReview contractTask = contractReviewService.getContractTask(task.getId());

        if (contractTask.getReviewConfiguration().isDraft()) {
            log.info("草稿任务暂不执行， 任务ID: {}", task.getId());
            return;
        }

        // 启动任务
        task.start();

        log.debug("开始执行任务 {} 的模型审查", task.getId());

        try {
            String prompt = arrangePrompt(contractTask);
            if (null == prompt) {
                log.error("没有合适的提示词用于模型审查，进行快速失败");
                throw new RuntimeException("没有合适的提示词用于模型审查");
            }

            // TODO OperationLog 替换日志并降低日志等级
            log.info("contractTask:{}, 模型审查提示词：{}", contractTask.getId(), prompt);

            // 执行AI审查
            ReviewResult reviewResult = performAIReview(task, contractTask, prompt);

            // 保存阶段结果（无论业务结果如何都保存）业务结果可能包含：风险等级、合规问题、通过/不通过等
            saveStageResult(task, contractTask, reviewResult);

            // 更新到下一阶段
            task.updateCurrentStage(ExecutionStage.REPORT_GENERATION);
            task.complete(); // 当前阶段完成（程序执行成功）

            log.info("任务 {} 模型审查阶段完成，已推进到报告生成阶段", task.getId());

        } catch (Exception e) {
            // 程序执行失败，标记任务为失败状态以触发重试
            task.fail("模型审查程序执行失败: " + e.getMessage());
            throw e; // 重新抛出异常，让上层处理
        } finally {
            taskRepository.save(task);
        }
    }

    private String arrangePrompt(ContractReview contractTask) {
        ReviewRuleQueryFeignDTO reviewRuleQueryFeignDTO = getReviewRuleQueryFeignDTO(contractTask);
        ReviewRulePageResultFeignDTO ruleResult =
            reviewRuleFeignClient.searchReviewRules(reviewRuleQueryFeignDTO);
        List<ReviewRuleFeignDTO> rules = ruleResult.getRecords();

        // 获取条款
        List<ClauseFeignDTO> clauses =
            clauseFeignClient.getClausesByContractId(contractTask.getContractId());

        PromptQueryFeignDTO queryFeignDTO = new PromptQueryFeignDTO();
        // 合同类型+提示词 即为提示词的模型审查命名规则 并且
//        queryFeignDTO.setKeyword(reviewConfiguration.getContractType() + "提示词");
        // FIXME 测试用
        queryFeignDTO.setPromptName("其他合同提示词");
        queryFeignDTO.setEnabled(true);
        queryFeignDTO.setPromptTypeList(List.of("INNER"));
        queryFeignDTO.setPageSize(Integer.MAX_VALUE);
        PromptPageResultFeignDTO systemPrompts = promptFeignClient.searchPrompts(queryFeignDTO);
        List<PromptFeignDTO> prompts = systemPrompts.getRecords();
        if (prompts == null || prompts.size() != 1) {
            log.error("模型审查查询提示词遇到错误");
            return null;
        }

        // 根据条款类型聚合条款和规则 TODO 缺少对于规则类型的支持 例如兜底类型需要在没有相关条款提示词的情况下使用
        Map<String, List<ClauseFeignDTO>> clausesByType = new java.util.HashMap<>();
        Map<String, List<ReviewRuleFeignDTO>> rulesByType = new java.util.HashMap<>();

        // 按条款类型聚合条款
        if (clauses != null && !clauses.isEmpty()) {
            for (ClauseFeignDTO clause : clauses) {
                String clauseType = clause.getClauseType();
                if (clauseType != null) {
                    clausesByType.computeIfAbsent(clauseType, k -> new ArrayList<>()).add(clause);
                }
            }
        }

        // 按条款类型聚合规则，一个规则可以应用在多个条款类型
        if (rules != null && !rules.isEmpty()) {
            for (ReviewRuleFeignDTO rule : rules) {
                List<String> applicableClauseTypes = rule.getApplicableClauseTypes();
                if (applicableClauseTypes != null && !applicableClauseTypes.isEmpty()) {
                    // 一个规则可以应用在多个条款类型中
                    for (String clauseType : applicableClauseTypes) {
                        if (clauseType != null) {
                            rulesByType.computeIfAbsent(clauseType, k -> new ArrayList<>()).add(rule);
                        }
                    }
                }
            }
        }

        // 构建动态提示词
        PromptFeignDTO systemPrompt = prompts.get(0);
        String promptContent = systemPrompt.getPromptContent();

        StringBuilder rulePrompt = new StringBuilder();
        StringBuilder clausePrompt = new StringBuilder();

        // 按条款类型组织条款信息
        for (Map.Entry<String, List<ClauseFeignDTO>> entry : clausesByType.entrySet()) {
            String clauseType = entry.getKey();
            List<ClauseFeignDTO> typeClauses = entry.getValue();

            rulePrompt.append(String.format("\n【%s条款】(%d条):\n", clauseType, typeClauses.size()));
            for (ClauseFeignDTO clause : typeClauses) {
                rulePrompt.append(String.format("-id: %s\n%s: %s\n",
                    clause.getId(),
                    clause.getClauseTitle() != null ? clause.getClauseTitle() : "无标题",
                    clause.getClauseContent() != null ? clause.getClauseContent() : "无内容"));
            }
        }

        // 按条款类型组织规则信息
        for (Map.Entry<String, List<ReviewRuleFeignDTO>> entry : rulesByType.entrySet()) {
            String ruleClauseType = entry.getKey();
            List<ReviewRuleFeignDTO> typeRules = entry.getValue();

            clausePrompt.append(String.format("\n【%s规则】(%d条):\n", ruleClauseType, typeRules.size()));
            for (ReviewRuleFeignDTO rule : typeRules) {
                clausePrompt.append(String.format("-id: %s\n%s: %s\n",
                    rule.getId(),
                    rule.getRuleName() != null ? rule.getRuleName() : "无名称",
                    rule.getRuleContent() != null ? rule.getRuleContent() : "无描述"));
            }
        }

        promptContent = promptContent.replace("</rules>", rulePrompt.toString());
        promptContent = promptContent.replace("</clauses>", clausePrompt.toString());

        return promptContent;
    }

    private static ReviewRuleQueryFeignDTO getReviewRuleQueryFeignDTO(ContractReview contractTask) {
        ReviewConfiguration reviewConfiguration = contractTask.getReviewConfiguration();
        // 审查规则, 根据合同类型、条款类型、模式编码、enable来筛选审查规则
        ReviewRuleQueryFeignDTO reviewRuleQueryFeignDTO = new ReviewRuleQueryFeignDTO();
        reviewRuleQueryFeignDTO.setContractType(reviewConfiguration.getContractType());
        reviewRuleQueryFeignDTO.setClauseType(reviewConfiguration.getContractType());
        reviewRuleQueryFeignDTO.setEnabled(true);
        reviewRuleQueryFeignDTO.setPromptModeCodeList(List.of(reviewConfiguration.getPromptTemplate().getCode()));
        reviewRuleQueryFeignDTO.setPageSize(Integer.MAX_VALUE);
        return reviewRuleQueryFeignDTO;
    }

    /**
     * 执行AI模型审查
     */
    private ReviewResult performAIReview(Task task, ContractReview contractTask, String prompt) {
        try {
            Long contractId = contractTask.getId();
            if (contractId == null) {
                throw new IllegalArgumentException("无法获取合同ID");
            }

            log.debug("调用AI审查服务处理合同 {}", contractId);

            /**
             * 标准 开启思考并且使用标准的提示词 TODO 审查合同条款外的项
             * 快速 TODO 关闭思考模式，提示词为快速版本提示词，只审查合同条款
             * 质量 TODO 开启思考模式，在报告生成前加一个复检流程
             */
            ChatRequest.Message message = ChatRequest.Message.textMessage("user", prompt);
            ArrayList<ChatRequest.Message> messages = Lists.newArrayList();
            messages.add(message);

            var response = aiClient.chat(ChatRequest.builder()
                .platform(PlatFormType.IFLOW)
                .model(ModelType.IFlow_GLM_4_6.getModelCode())
                .maxTokens(40960)
                .responseReformat(ChatRequest.ResponseReformat.builder().type("json").build())
                .messages(messages)
                .build());

            if (response == null || response.getData() == null ||
                response.getData().getMessages() == null || response.getData().getMessages().isEmpty()) {
                throw new RuntimeException("模型审查: AI模型响应为空");
            }

            String rawResult = response.getData().getMessages().get(0).getContent();

            if (rawResult == null || rawResult.trim().isEmpty()) {
                throw new RuntimeException("模型审查: AI模型返回的内容为空");
            }

            // 反序列化AI响应
            ReviewResult modelReviewResult = parseAIResponse(task, contractTask, rawResult);

            log.debug("合同 {} AI审查完成，总体风险等级: {}", contractId, modelReviewResult.getOverallRiskLevel());
            return modelReviewResult;

        } catch (JsonProcessingException e) {
            log.error("AI模型响应JSON解析失败: {}", e.getMessage(), e);
            throw new RuntimeException("AI模型响应格式错误，无法解析: " + e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            log.error("AI模型响应参数错误: {}", e.getMessage(), e);
            throw new RuntimeException("AI模型响应参数错误: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("AI模型审查失败: {}", e.getMessage(), e);
            throw new RuntimeException("AI模型审查失败: " + e.getMessage(), e);
        }
    }

    private ReviewResult fillModelReviewResult(ReviewResult modelReviewResult, Task task, ContractReview contractTask) {
        ReviewConfiguration reviewConfiguration = contractTask.getReviewConfiguration();
        modelReviewResult.setTaskId(task.getId().getValue());
        modelReviewResult.setContractId(contractTask.getContractId());
        // 目前写死的
        modelReviewResult.setModelVersion(ModelType.IFlow_GLM_4_6.getModelCode());
        modelReviewResult.setReviewType(reviewConfiguration.getReviewType().getDisplayName());

        return modelReviewResult;
    }

    /**
     * 解析AI响应JSON为ReviewResult对象
     */
    private ReviewResult parseAIResponse(Task task, ContractReview contractTask, String rawResult) throws JsonProcessingException {
        try {
            Long taskId = task.getId().getValue();
            Long contractId = contractTask.getContractId();
            // 解析JSON响应
            Map<String, Object> responseMap = objectMapper.readValue(rawResult, new TypeReference<Map<String, Object>>() {});

            // 创建基础ReviewResult对象
            ReviewResult reviewResult = ReviewResult.builder()
                .taskId(taskId)
                .contractId(contractId)
                .reviewType(ReviewType.MODEL_REVIEW.getDisplayName())
                .build();

            // 解析各个字段
            if (responseMap.containsKey("overallRiskLevel")) {
                String riskLevelStr = String.valueOf(responseMap.get("overallRiskLevel"));
                try {
                    RiskLevel.valueOf(riskLevelStr.toUpperCase());
                    reviewResult.setOverallRiskLevel(riskLevelStr);
                } catch (IllegalArgumentException e) {
                    log.warn("无效的风险等级值: {}, 使用默认值", riskLevelStr);
                    reviewResult.setOverallRiskLevel("MEDIUM");
                }
            }

            // confidence字段已被删除，不再处理

            if (responseMap.containsKey("summary")) {
                String summary = String.valueOf(responseMap.get("summary"));
                if (summary.length() > 2000) {
                    log.warn("摘要过长，截断至2000字符");
                    summary = summary.substring(0, 2000);
                }
                reviewResult.setSummary(summary);
            }

            // 设置阶段结果
            reviewResult.setStageResult("模型审查完成");

            fillModelReviewResult(reviewResult, task, contractTask);
            return reviewResult;

        } catch (JsonProcessingException e) {
            log.error("JSON解析失败，原始响应: {}", rawResult, e);
            throw new RuntimeException("无法解析AI响应JSON: " + e.getMessage(), e);
        }
    }


    /**
     * 保存阶段结果
     * 保存模型审查阶段的ReviewResult到数据库
     */
    private void saveStageResult(Task task, ContractReview contractTask, ReviewResult reviewResult) {
        try {
            log.debug("保存任务 {} 的模型审查阶段结果", task.getId());

            // 调用应用服务层保存审查结果
            contractReviewService.saveReviewResult(reviewResult);

            log.info("成功保存任务 {} 的模型审查结果，总体风险等级: {}",
                task.getId(), reviewResult.getOverallRiskLevel());

        } catch (Exception e) {
            log.error("保存阶段结果失败: {}", e.getMessage(), e);
            // 阶段结果保存失败不应该影响主流程
            // 但需要记录详细的错误信息以便问题排查
            throw new RuntimeException("保存审查结果失败: " + e.getMessage(), e);
        }
    }

    /**
     * 处理程序执行失败（需要重试的情况）
     * 业务结果说明：
     * - AI审查发现风险问题 → 保存结果，继续下一阶段（报告生成）
     * - AI审查通过 → 保存结果，继续下一阶段
     * - AI服务调用失败 → 程序异常，触发重试
     * - 网络错误 → 程序异常，触发重试
     */
    private void handleTaskExecutionFailure(Task task, Exception e) {
        log.error("任务 {} 执行失败: {}", task.getId(), e.getMessage(), e);

        try {
            // 任务状态设为FAILED，触发重试机制
            task.fail("程序执行失败: " + e.getMessage());
            taskRepository.save(task);

        } catch (Exception saveException) {
            log.error("保存任务失败状态时发生异常: {}", saveException.getMessage());
            // 如果保存失败，记录日志但不抛出异常，避免影响其他任务处理
        }
    }
}