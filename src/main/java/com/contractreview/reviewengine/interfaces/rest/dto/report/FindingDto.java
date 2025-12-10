package com.contractreview.reviewengine.interfaces.rest.dto.report;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 发现内容DTO
 *
 * @author SaltyFish
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "发现内容")
public class FindingDto {

    @Schema(description = "发现ID")
    private String id;

    @Schema(description = "发现类型: risk/warning/info/suggestion")
    private String type;

    @Schema(description = "发现描述")
    private String description;

    @Schema(description = "严重程度: high/medium/low")
    private String severity;

    @Schema(description = "关联的证据ID列表")
    private List<String> evidence;

    @Schema(description = "原文位置信息")
    private LocationDto location;

    @Schema(description = "相关条款原文")
    private String clauseText;
}