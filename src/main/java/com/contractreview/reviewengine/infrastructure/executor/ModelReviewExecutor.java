package com.contractreview.reviewengine.infrastructure.executor;

import com.alibaba.nacos.shaded.com.google.common.collect.Lists;
import com.contract.ai.feign.client.AiClient;
import com.contract.ai.feign.dto.ChatRequest;
import com.contract.ai.feign.dto.ChatResponse;
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
import com.contractreview.reviewengine.domain.model.ContractReview;
import com.contractreview.reviewengine.domain.model.ReviewResult;
import com.contractreview.reviewengine.domain.model.ReviewType;
import com.contractreview.reviewengine.domain.model.Task;
import com.contractreview.reviewengine.domain.repository.TaskRepository;
import com.contractreview.reviewengine.domain.valueobject.ReviewConfiguration;
import com.contractreview.reviewengine.domain.valueobject.RiskItem;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
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
                log.info("未实现模型审查");
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
        // 启动任务
        task.start();
        taskRepository.save(task);

        log.debug("开始执行任务 {} 的模型审查", task.getId());

        try {
            ContractReview contractTask = contractReviewService.getContractTask(task.getId());
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
            taskRepository.save(task);

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
        String prompt = "";
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
        queryFeignDTO.setKeyword("其他合同提示词");
        queryFeignDTO.setEnabled(true);
        queryFeignDTO.setPromptTypeList(List.of("INNER"));
        queryFeignDTO.setPageSize(Integer.MAX_VALUE);
        PromptPageResultFeignDTO systemPrompts = promptFeignClient.searchPrompts(queryFeignDTO);
        List<PromptFeignDTO> prompts = systemPrompts.getRecords();
        if (prompts == null || prompts.size() != 1) {
            log.error("模型审查查询提示词遇到错误");
            return null;
        }

        // 根据条款类型聚合条款和规则
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

        StringBuilder dynamicPrompt = new StringBuilder(promptContent);
        dynamicPrompt.append("\n\n=== 合同条款信息 ===\n");

        // 按条款类型组织条款信息
        for (Map.Entry<String, List<ClauseFeignDTO>> entry : clausesByType.entrySet()) {
            String clauseType = entry.getKey();
            List<ClauseFeignDTO> typeClauses = entry.getValue();

            dynamicPrompt.append(String.format("\n【%s条款】(%d条):\n", clauseType, typeClauses.size()));
            for (ClauseFeignDTO clause : typeClauses) {
                dynamicPrompt.append(String.format("- %s: %s\n",
                    clause.getClauseTitle() != null ? clause.getClauseTitle() : "无标题",
                    clause.getClauseContent() != null ? clause.getClauseContent() : "无内容"));
            }
        }

        dynamicPrompt.append("\n=== 审查规则 ===\n");

        // 按条款类型组织规则信息
        for (Map.Entry<String, List<ReviewRuleFeignDTO>> entry : rulesByType.entrySet()) {
            String ruleClauseType = entry.getKey();
            List<ReviewRuleFeignDTO> typeRules = entry.getValue();

            dynamicPrompt.append(String.format("\n【%s规则】(%d条):\n", ruleClauseType, typeRules.size()));
            for (ReviewRuleFeignDTO rule : typeRules) {
                dynamicPrompt.append(String.format("- %s: %s\n",
                    rule.getRuleName() != null ? rule.getRuleName() : "无名称",
                    rule.getRuleContent() != null ? rule.getRuleContent() : "无描述"));
            }
        }

        prompt = dynamicPrompt.toString();

        return prompt;
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

            // TODO 调控二元变量：速度 质量
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
            ReviewResult modelReviewResult = parseAIResponse(task.getId().getValue(), contractId, rawResult);

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

    /**
     * 解析AI响应JSON为ReviewResult对象
     */
    private ReviewResult parseAIResponse(Long taskId, Long contractId, String rawResult) throws JsonProcessingException {
        try {
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

            if (responseMap.containsKey("confidence")) {
                try {
                    Double confidence = Double.valueOf(String.valueOf(responseMap.get("confidence")));
                    if (confidence >= 0.0 && confidence <= 1.0) {
                        reviewResult.setConfidence(confidence);
                    } else {
                        log.warn("置信度值超出范围[0,1]: {}, 使用默认值", confidence);
                        reviewResult.setConfidence(0.5);
                    }
                } catch (NumberFormatException e) {
                    log.warn("置信度格式错误: {}, 使用默认值", responseMap.get("confidence"));
                    reviewResult.setConfidence(0.5);
                }
            }

            if (responseMap.containsKey("summary")) {
                String summary = String.valueOf(responseMap.get("summary"));
                if (summary.length() > 2000) {
                    log.warn("摘要过长，截断至2000字符");
                    summary = summary.substring(0, 2000);
                }
                reviewResult.setSummary(summary);
            }

            if (responseMap.containsKey("recommendations")) {
                String recommendations = String.valueOf(responseMap.get("recommendations"));
                if (recommendations.length() > 2000) {
                    log.warn("建议过长，截断至2000字符");
                    recommendations = recommendations.substring(0, 2000);
                }
                reviewResult.setRecommendations(recommendations);
            }

            // 解析风险项列表
            if (responseMap.containsKey("riskItems")) {
                try {
                    List<Map<String, Object>> riskItemsMap = objectMapper.convertValue(
                        responseMap.get("riskItems"),
                        new TypeReference<List<Map<String, Object>>>() {}
                    );

                    List<RiskItem> riskItems = new ArrayList<>();
                    for (Map<String, Object> riskItemMap : riskItemsMap) {
                        RiskItem riskItem = parseRiskItem(riskItemMap);
                        if (riskItem != null) {
                            riskItems.add(riskItem);
                        }
                    }
                    reviewResult.setRiskItems(riskItems);

                } catch (Exception e) {
                    log.warn("解析风险项失败: {}, 使用空列表", e.getMessage());
                    reviewResult.setRiskItems(new ArrayList<>());
                }
            }

            // 解析合规问题
            if (responseMap.containsKey("complianceIssues")) {
                try {
                    Map<String, Object> complianceIssues = objectMapper.convertValue(
                        responseMap.get("complianceIssues"),
                        new TypeReference<Map<String, Object>>() {}
                    );
                    reviewResult.setComplianceIssues(complianceIssues);
                } catch (Exception e) {
                    log.warn("解析合规问题失败: {}, 使用空Map", e.getMessage());
                    reviewResult.setComplianceIssues(new java.util.HashMap<>());
                }
            }

            // 设置阶段结果
            reviewResult.setStageResult("模型审查完成");

            return reviewResult;

        } catch (JsonProcessingException e) {
            log.error("JSON解析失败，原始响应: {}", rawResult, e);
            throw new RuntimeException("无法解析AI响应JSON: " + e.getMessage(), e);
        }
    }

    /**
     * 解析单个风险项
     */
    private RiskItem parseRiskItem(Map<String, Object> riskItemMap) {
        try {
            String factorName = riskItemMap.containsKey("factorName") ?
                String.valueOf(riskItemMap.get("factorName")) : "未知风险";

            RiskLevel riskLevel = RiskLevel.MEDIUM; // 默认中等风险
            if (riskItemMap.containsKey("riskLevel")) {
                String riskLevelStr = String.valueOf(riskItemMap.get("riskLevel"));
                try {
                    riskLevel = RiskLevel.valueOf(riskLevelStr.toUpperCase());
                } catch (IllegalArgumentException e) {
                    log.warn("无效的风险等级: {}, 使用默认MEDIUM", riskLevelStr);
                }
            }

            Double riskScore = 60.0; // 默认分数
            if (riskItemMap.containsKey("riskScore")) {
                try {
                    riskScore = Double.valueOf(String.valueOf(riskItemMap.get("riskScore")));
                    if (riskScore < 0.0 || riskScore > 100.0) {
                        log.warn("风险分数超出范围[0,100]: {}, 使用默认60", riskScore);
                        riskScore = 60.0;
                    }
                } catch (NumberFormatException e) {
                    log.warn("风险分数格式错误: {}, 使用默认60", riskItemMap.get("riskScore"));
                }
            }

            Double confidence = 0.8; // 默认置信度
            if (riskItemMap.containsKey("confidence")) {
                try {
                    confidence = Double.valueOf(String.valueOf(riskItemMap.get("confidence")));
                    if (confidence < 0.0 || confidence > 1.0) {
                        log.warn("置信度超出范围[0,1]: {}, 使用默认0.8", confidence);
                        confidence = 0.8;
                    }
                } catch (NumberFormatException e) {
                    log.warn("置信度格式错误: {}, 使用默认0.8", riskItemMap.get("confidence"));
                }
            }

            String riskSummary = riskItemMap.containsKey("riskSummary") ?
                String.valueOf(riskItemMap.get("riskSummary")) : "";
            if (riskSummary.length() > 100) {
                riskSummary = riskSummary.substring(0, 100);
            }

            String recommendation = riskItemMap.containsKey("recommendation") ?
                String.valueOf(riskItemMap.get("recommendation")) : "";
            if (recommendation.length() > 500) {
                recommendation = recommendation.substring(0, 500);
            }

            Long riskClauseId = null;
            if (riskItemMap.containsKey("riskClauseId")) {
                try {
                    riskClauseId = Long.valueOf(String.valueOf(riskItemMap.get("riskClauseId")));
                } catch (NumberFormatException e) {
                    log.debug("风险条款ID格式错误，忽略: {}", riskItemMap.get("riskClauseId"));
                }
            }

            String originContractText = riskItemMap.containsKey("originContractText") ?
                String.valueOf(riskItemMap.get("originContractText")) : null;
            if (originContractText != null && originContractText.length() > 1000) {
                originContractText = originContractText.substring(0, 1000);
            }

            return RiskItem.builder()
                .factorName(factorName)
                .riskLevel(riskLevel)
                .riskScore(riskScore)
                .confidence(confidence)
                .riskSummary(riskSummary)
                .recommendation(recommendation)
                .riskClauseId(riskClauseId)
                .originContractText(originContractText)
                .build();

        } catch (Exception e) {
            log.warn("解析风险项失败: {}, 跳过此项", e.getMessage());
            return null;
        }
    }


    /**
     * 保存阶段结果
     */
    private void saveStageResult(Task task, ContractReview contractTask, ReviewResult reviewResult) {
        try {
            log.debug("保存任务 {} 的模型审查阶段结果", task.getId());
            // 这里需要根据实际的阶段结果存储逻辑来实现
            // 可以将结果存储到review_result表中
            // 暂时只记录日志
        } catch (Exception e) {
            log.warn("保存阶段结果失败: {}", e.getMessage());
            // 阶段结果保存失败不应该影响主流程
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