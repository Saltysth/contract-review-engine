package com.contractreview.reviewengine.application.service;

import com.contractreview.reviewengine.domain.model.ReviewResult;
import com.contractreview.reviewengine.domain.model.TaskId;
import com.contractreview.reviewengine.domain.service.ReviewResultService;
import com.contractreview.reviewengine.interfaces.rest.dto.ReportDetailDto;
import com.contractreview.reviewengine.interfaces.rest.dto.report.*;
import com.contractreview.reviewengine.domain.valueobject.Evidence;
import com.contractreview.reviewengine.domain.exception.BusinessException;
import com.contract.common.feign.ClauseFeignClient;
import com.contract.common.feign.dto.ClauseFeignDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 报告服务
 * 负责组装报告详情数据
 *
 * @author SaltyFish
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReviewResultService reviewResultService;
    private final ContractReviewService contractReviewService;
    private final ClauseFeignClient clauseFeignClient;

    /**
     * 根据任务ID获取报告详情
     *
     * @param taskId 任务ID
     * @return 报告详情
     */
    public ReportDetailDto getReportDetail(TaskId taskId) {
        log.debug("获取任务{}的报告详情", taskId.getValue());

        // 获取审查结果
        Optional<ReviewResult> reviewResultOpt = reviewResultService.getReviewResultByTaskId(taskId);
        if (reviewResultOpt.isEmpty()) {
            throw new BusinessException("报告不存在", "DATA001");
        }

        ReviewResult reviewResult = reviewResultOpt.get();

        // 获取合同标题
        String contractTitle = contractReviewService.getContractTitleByTaskId(taskId);

        // 组装报告数据
        return assembleReportDetail(reviewResult, contractTitle);
    }

    /**
     * 组装报告详情数据
     */
    private ReportDetailDto assembleReportDetail(ReviewResult reviewResult, String contractTitle) {
        // 组装统计数据
        StatisticsDto statistics = assembleStatistics(reviewResult);

        // 组装规则结果
        List<RuleResultDto> ruleResults = assembleRuleResults(reviewResult);

        // 组装证据
        Map<String, EvidenceDto> evidences = assembleEvidences(reviewResult);

        return ReportDetailDto.builder()
                .id("report-" + reviewResult.getId())
                .taskId(reviewResult.getTaskId())
                .contractTitle(contractTitle)
                .riskLevel(reviewResult.getOverallRiskLevel())
                .summary(reviewResult.getSummary())
                .createdAt(reviewResult.getCreatedTime())
                .updatedAt(reviewResult.getCreatedTime()) // 暂时使用创建时间
                .statistics(statistics)
                .ruleResults(ruleResults)
                .evidences(evidences)
                .build();
    }

    /**
     * 组装统计数据
     */
    private StatisticsDto assembleStatistics(ReviewResult reviewResult) {
        // 获取条款总数
        int totalClauses = 0;
        try {
            List<ClauseFeignDTO> clauses = clauseFeignClient.getClausesByContractId(reviewResult.getContractId());
            totalClauses = clauses != null ? clauses.size() : 0;
        } catch (Exception e) {
            log.warn("获取合同{}的条款列表失败: {}", reviewResult.getContractId(), e.getMessage());
        }

        // 统计各风险等级数量
        // 注意：key的名称按照RiskLevel中的定义，之后加注释提示不允许改动
        Map<String, Long> riskCount = new HashMap<>();
        riskCount.put("HIGH", 0L);       // 对应RiskLevel.HIGH
        riskCount.put("MEDIUM", 0L);     // 对应RiskLevel.MEDIUM
        riskCount.put("LOW", 0L);        // 对应RiskLevel.LOW
        riskCount.put("NO_RISK", 0L);    // 对应RiskLevel.NO_RISK
        riskCount.put("CRITICAL", 0L);   // 对应RiskLevel.CRITICAL

        if (reviewResult.getRuleResults() != null) {
            reviewResult.getRuleResults().forEach(rule -> {
                if (rule.getRiskLevel() != null) {
                    String riskLevel = rule.getRiskLevel().name();
                    riskCount.put(riskLevel, riskCount.getOrDefault(riskLevel, 0L) + 1);
                }
            });
        }

        // 提取关键发现和建议
        List<String> keyFindings = new ArrayList<>();
        List<String> recommendations = new ArrayList<>();

        if (reviewResult.getKeyPoints() != null) {
            reviewResult.getKeyPoints().forEach(keyPoint -> {
                keyFindings.add(keyPoint.getPoint());
                recommendations.addAll(keyPoint.getRemediationSuggestions());
            });
        }

        return StatisticsDto.builder()
                .totalClauses(totalClauses)
                .reviewed(totalClauses) // 目前粒度粗，这里同上
                .critical(riskCount.get("CRITICAL").intValue())
                .high(riskCount.get("HIGH").intValue())      // 对应RiskLevel.HIGH
                .medium(riskCount.get("MEDIUM").intValue())  // 对应RiskLevel.MEDIUM
                .low(riskCount.get("LOW").intValue())        // 对应RiskLevel.LOW
                .noIssue(riskCount.get("NO_RISK").intValue() + riskCount.get("CRITICAL").intValue()) // NO_RISK和CRITICAL都计入noIssue
                .keyFindings(keyFindings)
                .recommendations(recommendations.stream().distinct().collect(Collectors.toList()))
                .build();
    }

    /**
     * 组装规则结果
     */
    private List<RuleResultDto> assembleRuleResults(ReviewResult reviewResult) {
        List<RuleResultDto> ruleResults = new ArrayList<>();

        // 添加概览规则结果
        RuleResultDto overview = RuleResultDto.builder()
                .id("overview")
                .title("审查概览")
                .kind("overview")
                .riskLevel(reviewResult.getOverallRiskLevel())
                .summary(reviewResult.getSummary())
                .findings(Collections.singletonList(
                    FindingDto.builder()
                        .id("finding-" + reviewResult.getId())
                        .type("info")
                        .description("合同结构完整，包含必要的基础条款")
                        .severity("low")
                        .evidence(Arrays.asList("structure-analysis", "completeness-check"))
                        .build()
                ))
                .recommendations(Arrays.asList(
                        "建议重点关注高风险条款的修改",
                        "考虑增加风险防控条款"
                ))
                .build();
        ruleResults.add(overview);

        // 获取条款列表用于填充clauseText
        Map<String, String> clauseContentMap = new HashMap<>();
        try {
            List<ClauseFeignDTO> clauses = clauseFeignClient.getClausesByContractId(reviewResult.getContractId());
            if (clauses != null) {
                clauseContentMap = clauses.stream()
                        .collect(Collectors.toMap(
                                clause -> String.valueOf(clause.getId()),
                                ClauseFeignDTO::getClauseContent,
                                (existing, replacement) -> existing
                        ));
            }
        } catch (Exception e) {
            log.warn("获取合同{}的条款内容失败: {}", reviewResult.getContractId(), e.getMessage());
        }

        // 添加规则结果
        if (reviewResult.getRuleResults() != null) {
            Map<String, String> finalClauseContentMap = clauseContentMap;
            reviewResult.getRuleResults().forEach(rule -> {
                List<FindingDto> findings = new ArrayList<>();
                if (rule.getFindings() != null) {
                    for (int i = 0; i < rule.getFindings().size(); i++) {
                        // 获取clauseText：优先从Feign获取，其次使用originContractText
                        String clauseText = null;
                        if (rule.getRiskClauseId() != null) {
                            clauseText = finalClauseContentMap.get(rule.getRiskClauseId());
                        }
                        if (clauseText == null) {
                            clauseText = rule.getOriginContractText();
                        }

                        findings.add(FindingDto.builder()
                                .id("finding-" + rule.getId() + "-" + i)
                                .type("risk")
                                .description(rule.getFindings().get(i))
                                .severity(rule.getRiskLevel().name().toLowerCase())
                                .evidence(List.of("rule-" + rule.getId()))
                                .location(null) // 暂不设置location字段
                                .clauseText(clauseText)
                                .build());
                    }
                }

                RuleResultDto ruleResult = RuleResultDto.builder()
                        .id("rule-" + rule.getId())
                        .title(rule.getRiskName())
                        .kind(rule.getRuleType())
                        .riskLevel(rule.getRiskLevel().name().toLowerCase())
                        .summary(rule.getSummary())
                        .findings(findings)
                        .recommendations(rule.getRecommendation())
                        .build();

                ruleResults.add(ruleResult);
            });
        }

        return ruleResults;
    }

    /**
     * 组装证据
     */
    private Map<String, EvidenceDto> assembleEvidences(ReviewResult reviewResult) {
        Map<String, EvidenceDto> evidences = new LinkedHashMap<>();

        // 添加系统规则证据
        evidences.put("structure-analysis", EvidenceDto.builder()
                .id("structure-analysis")
                .title("结构分析")
                .type("analysis")
                .description("合同结构的系统性分析")
                .content("通过对合同条款结构的分析，确保合同包含必要的法律要素和商业条款")
                .source("ai-analysis")
                .modelVersion(reviewResult.getModelVersion())
                .build());

        evidences.put("completeness-check", EvidenceDto.builder()
                .id("completeness-check")
                .title("完整性检查")
                .type("rule")
                .description("合同条款完整性检查")
                .content("检查合同是否包含所有必要的条款，包括但不限于：当事人信息、标的、价格、履行期限、违约责任等")
                .source("system-rules")
                .version("1.0")
                .build());

        // 添加规则相关证据
        if (reviewResult.getRuleResults() != null) {
            reviewResult.getRuleResults().forEach(rule -> {
                String evidenceId = "rule-" + rule.getId();
                evidences.put(evidenceId, EvidenceDto.builder()
                        .id(evidenceId)
                        .title(rule.getRiskName() + "规则")
                        .type("rule")
                        .description("系统内置规则：" + rule.getSummary())
                        .content("根据行业最佳实践和法律法规要求，对" + rule.getRiskName() + "进行审查")
                        .source("system-rules")
                        .version("1.0")
                        .build());
            });
        }

        // 添加证据库中的证据
        if (reviewResult.getEvidences() != null) {
            reviewResult.getEvidences().forEach(evidence -> {
                String evidenceId = "evidence-" + evidence.getTitle().hashCode();
                evidences.put(evidenceId, EvidenceDto.builder()
                        .id(evidenceId)
                        .title(evidence.getTitle())
                        .type(evidence.getType().name().toLowerCase())
                        .description(evidence.getTitle())
                        .content(evidence.getContent())
                        .source(evidence.getType() == Evidence.EvidenceType.RULE ? "system-rules" : "legal-knowledge-base")
                        .industry(evidence.getIndustry())
                        .references(evidence.getReferences())
                        .build());
            });
        }

        return evidences;
    }
}