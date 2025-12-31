package com.contractreview.reviewengine.infrastructure.executor;

import com.alibaba.nacos.shaded.com.google.common.collect.Lists;
import com.contract.ai.feign.client.AiClient;
import com.contract.ai.feign.dto.ChatRequest;
import com.contract.ai.feign.enums.ModelType;
import com.contract.ai.feign.enums.PlatFormType;
import com.contract.common.enums.ReviewTypeDetail;
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
import com.contractreview.reviewengine.domain.model.ReviewRuleResultEntity;
import com.contractreview.reviewengine.domain.model.Task;
import com.contractreview.reviewengine.domain.repository.TaskRepository;
import com.contractreview.reviewengine.domain.valueobject.Evidence;
import com.contractreview.reviewengine.domain.valueobject.KeyPoint;
import com.contractreview.reviewengine.domain.valueobject.ReviewConfiguration;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
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

    @Value("${ruoyi.remote-auth.secret:}")
    private String secret;

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

        if (task.getConfiguration().getIsDraft()) {
            log.info("草稿任务暂不执行， 任务ID: {}", task.getId());
            return;
        }

        // 启动任务
        task.start();
        taskRepository.save(task);

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
            reviewRuleFeignClient.searchReviewRules(reviewRuleQueryFeignDTO, secret);
        List<ReviewRuleFeignDTO> rules = ruleResult.getRecords();

        // 获取条款
        List<ClauseFeignDTO> clauses =
            clauseFeignClient.getClausesByContractId(contractTask.getContractId(), secret);

        PromptQueryFeignDTO queryFeignDTO = new PromptQueryFeignDTO();
        // 合同类型+提示词 即为提示词的模型审查命名规则 并且
//        queryFeignDTO.setKeyword(reviewConfiguration.getContractType() + "提示词");
        // FIXME 测试用
        queryFeignDTO.setPromptName("其他合同提示词");
        queryFeignDTO.setEnabled(true);
        queryFeignDTO.setPromptTypeList(List.of("INNER"));
        queryFeignDTO.setPageSize(Integer.MAX_VALUE);
        PromptPageResultFeignDTO systemPrompts = promptFeignClient.searchPrompts(queryFeignDTO, secret);
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

            clausePrompt.append(String.format("\n【%s条款】(%d条):\n", clauseType, typeClauses.size()));
            for (ClauseFeignDTO clause : typeClauses) {
                clausePrompt.append(String.format("-id: %s\n%s: %s\n",
                    clause.getId(),
                    clause.getClauseTitle() != null ? clause.getClauseTitle() : "无标题",
                    clause.getClauseContent() != null ? clause.getClauseContent() : "无内容"));
            }
        }

        // 按条款类型组织规则信息
        for (Map.Entry<String, List<ReviewRuleFeignDTO>> entry : rulesByType.entrySet()) {
            String ruleClauseType = entry.getKey();
            List<ReviewRuleFeignDTO> typeRules = entry.getValue();

            rulePrompt.append(String.format("\n【%s规则】(%d条):\n", typeRules.get(0).getRuleTypeDescription(), typeRules.size()));
            for (ReviewRuleFeignDTO rule : typeRules) {
                rulePrompt.append(String.format("-id: %s\n%s: %s\n",
                    rule.getId(),
                    rule.getRuleName() != null ? rule.getRuleName() : "无名称",
                    rule.getRuleContent() != null ? rule.getRuleContent() : "无描述"));
            }
        }

        promptContent = promptContent.replace("</rules>", rulePrompt.toString());
        promptContent = promptContent.replace("</clauses>", clausePrompt.toString());
        promptContent = promptContent.replace("</industry>", contractTask.getReviewConfiguration().getIndustry());
        promptContent = promptContent.replace("</currency>", contractTask.getReviewConfiguration().getCurrency());
        promptContent = promptContent.replace("</RiskLevel>", Arrays.toString(RiskLevel.values()));
        promptContent = promptContent.replace("</ReviewTypeDetail>", Arrays.toString(ReviewTypeDetail.values()));
        promptContent = promptContent.replace("</EvidenceType>", Arrays.toString(Evidence.EvidenceType.values()));

        return promptContent;
    }

    public static void main(String[] args) {
        System.out.println(Arrays.toString(RiskLevel.values()));

        // 测试JSON反序列化
        String testJson = "{ \"overallRiskLevel\": \"HIGH\", \"summary\": \"合同整体风险高，存在多项违法及显失公平条款\", \"ruleResults\": [ { \"riskName\": \"逾期付款违约金条款无效风险\", \"ruleType\": \"PAYMENT_TERM\", \"riskLevel\": \"HIGH\", \"riskScore\": 95.00, \"summary\": \"逾期付款无违约金，条款形同虚设\", \"findings\": [ \"约定逾期超过0日支付违约金\", \"约定违约金标准为万分之0\", \"条款无实际约束力\" ], \"recommendation\": [ \"建议修改为：'乙方未按约支付租金的，逾期超过3日后，应每日向甲方支付所欠租金总额的万分之五（0.05%）作为违约金。'\", \"依据审查规则第1条支付风险评估\" ], \"riskClauseId\": \"7\", \"originContractText\": \"\" }, { \"riskName\": \"违法断电条款风险\", \"ruleType\": \"LIQUIDATED_DAMAGES\", \"riskLevel\": \"HIGH\", \"riskScore\": 99.00, \"summary\": \"甲方无权擅自切断乙方电源\", \"findings\": [ \"条款约定甲方可对欠费乙方切断电源\", \"该条款侵犯了乙方基本居住权利\", \"该行为违反了相关法律规定\" ], \"recommendation\": [ \"建议完全删除'乙方存在应交未交账单的，甲方有权切断房屋电源'的内容\", \"修改为：'乙方逾期支付租金及其他费用的，甲方有权依据合同约定追究其违约责任。'\" ], \"riskClauseId\": \"27\", \"originContractText\": \"\" }, { \"riskName\": \"拆迁补偿权放弃条款无效风险\", \"ruleType\": \"LIQUIDATED_DAMAGES\", \"riskLevel\": \"HIGH\", \"riskScore\": 90.00, \"summary\": \"乙方放弃法定拆迁补偿的条款无效\", \"findings\": [ \"约定乙方放弃所有拆迁补偿费用\", \"包括货币补偿、临时安置补助等\", \"该条款排除了乙方主要法定权利\" ], \"recommendation\": [ \"建议删除'乙方承诺放弃承租人所享有的货币补偿金、临时安置补助费等所有费用'的内容\", \"或增加约定'因拆迁导致合同终止的，甲方应给予乙方相当于一个月租金的搬迁补偿'\" ], \"riskClauseId\": \"19\", \"originContractText\": \"\" }, { \"riskName\": \"争议解决方式约定不明风险\", \"ruleType\": \"DISPUTE_RESOLUTION\", \"riskLevel\": \"MEDIUM\", \"riskScore\": 70.00, \"summary\": \"争议解决方式未明确选择其一\", \"findings\": [ \"同时约定'向法院起诉'和'申请仲裁'\", \"'或'字选择导致条款无效\", \"争议发生时可能因管辖权问题产生额外纠纷\" ], \"recommendation\": [ \"建议明确选择一种争议解决方式，例如修改为：'依法向房屋所在地有管辖权的人民法院起诉'\", \"或修改为：'提交上海仲裁委员会，按照该会届时有效的仲裁规则进行仲裁'\" ], \"riskClauseId\": \"26\", \"originContractText\": \"\" } ], \"keyPoints\": [ { \"point\": \"条款7约定逾期付款违约金为'万分之0'，实际无任何惩罚效力，无法保障甲方收款权利。\", \"type\": \"违约金无效\", \"remediationSuggestions\": [ \"建议修改为：'乙方未按约支付租金的，逾期超过3日后，应每日向甲方支付所欠租金总额的万分之五（0.05%）作为违约金。'\" ], \"riskLevel\": \"HIGH\", \"reviewRuleId\": 1, \"clauseIds\": \"7\" }, { \"point\": \"条款27赋予甲方在乙方欠费时擅自断电的权利，该条款因违反法律强制性规定而自始无效。\", \"type\": \"违法条款\", \"remediationSuggestions\": [ \"建议完全删除'乙方存在应交未交账单的，甲方有权切断房屋电源'的内容，通过合法途径追缴欠费。\" ], \"riskLevel\": \"HIGH\", \"reviewRuleId\": 11, \"clauseIds\": \"27\" }, { \"point\": \"条款19约定乙方放弃法定拆迁补偿，该格式条款排除了对方主要权利，应属无效。\", \"type\": \"权利限制\", \"remediationSuggestions\": [ \"建议删除'乙方承诺放弃承租人所享有的货币补偿金...'的内容，或明确约定甲方对乙方的具体搬迁补偿标准。\" ], \"riskLevel\": \"HIGH\", \"reviewRuleId\": 2, \"clauseIds\": \"19\" }, { \"point\": \"条款26对诉讼和仲裁约定'或'选择，根据法律规定，此种争议解决协议无效。\", \"type\": \"争议解决\", \"remediationSuggestions\": [ \"建议仅保留一种争议解决方式，例如：'向房屋所在地人民法院起诉'，确保条款的有效性。\" ], \"riskLevel\": \"MEDIUM\", \"reviewRuleId\": 10, \"clauseIds\": \"26\" } ], \"evidences\": [ { \"title\": \"付款条款风险评估\", \"type\": \"rule\", \"content\": \"依据《民法典》第585条，违约金应以补偿性为主，惩罚性为辅。条款7约定的违约金为0，无法起到督促履约和弥补损失的作用，应予修正。\", \"references\": [ \"片段: 条款7\" ] }, { \"title\": \"违法条款审查\", \"type\": \"rule\", \"content\": \"依据《民法典》第654条及《电力供应与使用条例》相关规定，供电行为须经法定程序，房东作为非供电主体，无权擅自对用户中断供电。条款27相关内容违反法律强制性规定，无效。\", \"references\": [ \"片段: 条款27\" ] }, { \"title\": \"拆迁补偿权审查\", \"type\": \"rule\", \"content\": \"依据《国有土地上房屋征收与补偿条例》第17条，因征收房屋造成搬迁的，房屋征收部门应当向被征收人支付搬迁费。承租人作为实际使用人，享有获得搬迁、临时安置补偿的法定权利。条款19约定乙方放弃该权利，属无效格式条款。\", \"references\": [ \"片段: 条款19\" ] }, { \"title\": \"争议解决条款审查\", \"type\": \"rule\", \"content\": \"依据《最高人民法院关于适用<中华人民共和国仲裁法>若干问题的解释》第7条，当事人约定争议可以向仲裁机构申请仲裁也可以向人民法院起诉的，仲裁协议无效。条款26的约定属此无效情形。\", \"references\": [ \"片段: 条款26\" ] } ]}";

        ObjectMapper mapper = new ObjectMapper();
        try {
            ReviewResult result = mapper.readValue(testJson, ReviewResult.class);
            System.out.println("反序列化成功！");
            System.out.println("OverallRiskLevel: " + result.getOverallRiskLevel());
            System.out.println("Summary: " + result.getSummary());
            System.out.println("RuleResults count: " + (result.getRuleResults() != null ? result.getRuleResults().size() : 0));
            System.out.println("KeyPoints count: " + (result.getKeyPoints() != null ? result.getKeyPoints().size() : 0));
            System.out.println("Evidences count: " + (result.getEvidences() != null ? result.getEvidences().size() : 0));

            // 打印第一个规则结果
            if (result.getRuleResults() != null && !result.getRuleResults().isEmpty()) {
                ReviewRuleResultEntity firstRule = result.getRuleResults().get(0);
                System.out.println("\n第一个规则结果:");
                System.out.println("  RiskName: " + firstRule.getRiskName());
                System.out.println("  RuleType: " + firstRule.getRuleType());
                System.out.println("  RiskLevel: " + firstRule.getRiskLevel());
                System.out.println("  RiskScore: " + firstRule.getRiskScore());
            }

            // 打印第一个关键点
            if (result.getKeyPoints() != null && !result.getKeyPoints().isEmpty()) {
                KeyPoint firstKeyPoint = result.getKeyPoints().get(0);
                System.out.println("\n第一个关键点:");
                System.out.println("  Point: " + firstKeyPoint.getPoint());
                System.out.println("  Type: " + firstKeyPoint.getType());
                System.out.println("  ClauseIds: " + firstKeyPoint.getClauseIds());
            }

            // 打印第一个证据
            if (result.getEvidences() != null && !result.getEvidences().isEmpty()) {
                Evidence firstEvidence = result.getEvidences().get(0);
                System.out.println("\n第一个证据:");
                System.out.println("  Title: " + firstEvidence.getTitle());
                System.out.println("  Type: " + firstEvidence.getType());
                System.out.println("  Content: " + firstEvidence.getContent());
            }

        } catch (Exception e) {
            System.err.println("反序列化失败: " + e.getMessage());
            e.printStackTrace();
        }
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
                .maxTokens(102400)
                .responseReformat(ChatRequest.ResponseReformat.builder().type("json").build())
                .messages(messages)
                .build(), secret);

            if (response == null || response.getData() == null ||
                response.getData().getMessages() == null || response.getData().getMessages().isEmpty()) {
                throw new RuntimeException("模型审查: AI模型响应为空");
            }

            String rawResult = response.getData().getMessages().get(0).getContent();

            if (rawResult == null || rawResult.trim().isEmpty()) {
                throw new RuntimeException("模型审查: AI模型返回的内容为空");
            }
            log.debug("rawResult is {}", rawResult);

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
        modelReviewResult.setStageResult("模型审查完成");

        return modelReviewResult;
    }

    /**
     * 解析AI响应JSON为ReviewResult对象
     */
    private ReviewResult parseAIResponse(Task task, ContractReview contractTask, String rawResult) throws JsonProcessingException {
        try {
            // 直接反序列化到ReviewResult对象
            // 使用@JsonIgnoreProperties(ignoreUnknown = true)忽略AI返回但ReviewResult中不存在的字段
            ReviewResult reviewResult = objectMapper.readValue(rawResult, ReviewResult.class);

            // 填充任务相关信息
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