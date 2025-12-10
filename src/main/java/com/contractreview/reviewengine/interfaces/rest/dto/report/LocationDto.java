package com.contractreview.reviewengine.interfaces.rest.dto.report;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 原文位置信息DTO
 *
 * @author SaltyFish
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "原文位置信息")
public class LocationDto {

    @Schema(description = "页码")
    private Integer page;

    @Schema(description = "条款编号或名称")
    private String clause;

    @Schema(description = "行号")
    private Integer line;
}