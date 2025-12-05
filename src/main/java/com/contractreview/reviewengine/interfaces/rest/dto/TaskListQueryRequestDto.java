package com.contractreview.reviewengine.interfaces.rest.dto;

import com.contractreview.reviewengine.domain.enums.ReviewType;
import com.contractreview.reviewengine.domain.enums.TaskStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 合同任务列表查询参数DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "合同任务列表查询参数")
public class TaskListQueryRequestDto {

    @Schema(description = "页码", example = "1", defaultValue = "1")
    private Integer pageNum = 1;

    @Schema(description = "每页大小", example = "10", defaultValue = "10")
    private Integer pageSize = 10;

    @Schema(description = "任务名称模糊查询", example = "测试合同")
    private String taskName;

    @Schema(description = "合同类型精准筛选", example = "FULL_REVIEW")
    private ReviewType contractType;

    @Schema(description = "任务状态精准筛选", example = "COMPLETED")
    private TaskStatus taskStatus;
}