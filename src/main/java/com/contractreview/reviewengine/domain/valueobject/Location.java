package com.contractreview.reviewengine.domain.valueobject;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 条款位置信息
 * 用于定位合同中的具体条款位置
 *
 * @author SaltyFish
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Location {

    /**
     * 章节编号
     */
    private String chapter;

    /**
     * 条款编号
     */
    private String clause;

    /**
     * 页码
     */
    private Integer page;

    /**
     * 开始行号
     */
    private Integer startLine;

    /**
     * 结束行号
     */
    private Integer endLine;

    /**
     * 开始字符位置（可选）
     */
    private Integer startChar;

    /**
     * 结束字符位置（可选）
     */
    private Integer endChar;

    /**
     * 章节标题
     */
    private String chapterTitle;

    /**
     * JSON格式的扩展位置信息（用于存储复杂的位置数据）
     */
    private String extendedLocation;
}