package com.contractreview.reviewengine.interfaces.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 合同任务列表响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "合同任务列表响应")
public class TaskListResponseDto {

    @Schema(description = "统计信息")
    private TaskListStatisticsDto statistics;

    @Schema(description = "任务列表")
    private List<ContractTaskListItemDto> tasks;

    @Schema(description = "总记录数")
    private Long total;

    @Schema(description = "当前页码")
    private Integer pageNum;

    @Schema(description = "每页大小")
    private Integer pageSize;

    @Schema(description = "总页数")
    private Integer totalPages;
}