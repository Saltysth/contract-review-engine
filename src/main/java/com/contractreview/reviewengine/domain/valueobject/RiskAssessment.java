package com.contractreview.reviewengine.domain.valueobject;

import com.contractreview.reviewengine.domain.enums.RiskLevel;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 风险评估值对象
 * 
 * @author SaltyFish
 */
@Value
@Builder
public class RiskAssessment {
    
    RiskLevel overallRiskLevel;
    BigDecimal riskScore;
    Double overallScore;
    List<RiskFactor> riskFactors;
    Map<String, RiskLevel> categoryRisks;
    String summary;
    List<String> recommendations;
    
    public RiskAssessment(RiskLevel overallRiskLevel, 
                         BigDecimal riskScore,
                         Double overallScore,
                         List<RiskFactor> riskFactors,
                         Map<String, RiskLevel> categoryRisks,
                         String summary,
                         List<String> recommendations) {
        this.overallRiskLevel = overallRiskLevel;
        this.riskScore = riskScore;
        this.overallScore = overallScore != null ? overallScore : (riskScore != null ? riskScore.doubleValue() : 0.0);
        this.riskFactors = riskFactors != null ? new ArrayList<>(riskFactors) : new ArrayList<>();
        this.categoryRisks = categoryRisks != null ? new HashMap<>(categoryRisks) : new HashMap<>();
        this.summary = summary != null ? summary : "风险评估摘要";
        this.recommendations = recommendations != null ? new ArrayList<>(recommendations) : new ArrayList<>();
    }
    
    /**
     * 计算风险评估
     */
    public static RiskAssessment calculateRisk(List<RiskFactor> factors) {
        if (factors == null || factors.isEmpty()) {
            return new RiskAssessment(RiskLevel.LOW, BigDecimal.ZERO, 0.0, new ArrayList<>(), new HashMap<>(), "无风险因子", new ArrayList<>());
        }
        
        // 计算总风险分数
        double totalScore = factors.stream()
                .mapToDouble(RiskFactor::getScore)
                .sum();
        
        BigDecimal totalScoreBD = BigDecimal.valueOf(totalScore);
        
        // 计算平均分数
        BigDecimal averageScore = totalScoreBD.divide(BigDecimal.valueOf(factors.size()), 2, BigDecimal.ROUND_HALF_UP);
        
        // 确定风险等级
        RiskLevel level = determineRiskLevel(averageScore);
        
        // 按类别分组风险
        Map<String, RiskLevel> categoryRisks = categorizeRisks(factors);
        
        return new RiskAssessment(level, averageScore, averageScore.doubleValue(), factors, categoryRisks, 
                "基于" + factors.size() + "个风险因子的评估，总体风险等级：" + level.name(), new ArrayList<>());
    }
    
    /**
     * 确定风险等级
     */
    private static RiskLevel determineRiskLevel(BigDecimal score) {
        if (score.compareTo(BigDecimal.valueOf(80)) >= 0) return RiskLevel.CRITICAL;
        if (score.compareTo(BigDecimal.valueOf(60)) >= 0) return RiskLevel.HIGH;
        if (score.compareTo(BigDecimal.valueOf(40)) >= 0) return RiskLevel.MEDIUM;
        return RiskLevel.LOW;
    }
    
    /**
     * 按类别分组风险
     */
    private static Map<String, RiskLevel> categorizeRisks(List<RiskFactor> factors) {
        Map<String, List<RiskFactor>> categoryMap = new HashMap<>();
        
        // 按类别分组
        for (RiskFactor factor : factors) {
            categoryMap.computeIfAbsent(factor.getType(), k -> new ArrayList<>()).add(factor);
        }
        
        // 计算每个类别的风险等级
        Map<String, RiskLevel> categoryRisks = new HashMap<>();
        for (Map.Entry<String, List<RiskFactor>> entry : categoryMap.entrySet()) {
            double categoryScoreDouble = entry.getValue().stream()
                    .mapToDouble(RiskFactor::getScore)
                    .average()
                    .orElse(0.0);
            
            BigDecimal categoryScore = BigDecimal.valueOf(categoryScoreDouble);
            
            categoryRisks.put(entry.getKey(), determineRiskLevel(categoryScore));
        }
        
        return categoryRisks;
    }
    
    /**
     * 检查是否为高风险
     */
    public boolean isHighRisk() {
        return overallRiskLevel.isHighRisk();
    }
    
    /**
     * 获取指定类别的风险等级
     */
    public RiskLevel getCategoryRisk(String category) {
        return categoryRisks.getOrDefault(category, RiskLevel.LOW);
    }
    
    /**
     * 获取总体风险分数
     */
    public double getOverallScore() {
        return riskScore.doubleValue();
    }
}