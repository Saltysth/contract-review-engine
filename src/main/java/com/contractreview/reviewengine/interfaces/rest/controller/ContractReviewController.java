package com.contractreview.reviewengine.interfaces.rest.controller;

import com.contract.common.feign.ContractFeignClient;
import com.contractreview.reviewengine.application.service.ContractReviewService;
import com.contractreview.reviewengine.application.service.ReportService;
import com.contractreview.reviewengine.domain.exception.BusinessException;
import com.contractreview.reviewengine.domain.model.ContractReview;
import com.contractreview.reviewengine.domain.model.TaskId;
import com.contractreview.reviewengine.interfaces.rest.dto.ApiResponse;
import com.contractreview.reviewengine.interfaces.rest.dto.ContractReviewCreateRequestDto;
import com.contractreview.reviewengine.interfaces.rest.dto.ContractReviewRequestDto;
import com.contractreview.reviewengine.interfaces.rest.dto.ContractTaskDetailDto;
import com.contractreview.reviewengine.interfaces.rest.dto.ContractTaskDto;
import com.contractreview.reviewengine.interfaces.rest.dto.ReportDetailDto;
import com.contractreview.reviewengine.interfaces.rest.dto.TaskListQueryRequestDto;
import com.contractreview.reviewengine.interfaces.rest.dto.TaskListResponseDto;
import com.contractreview.reviewengine.interfaces.rest.dto.TaskProgressDto;
import com.contractreview.reviewengine.interfaces.rest.mapper.ContractTaskMapper;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.feign.annotation.RemotePreAuthorize;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 合同审查REST控制器
 */
@RestController
@RequestMapping("/api/v1/contract-review")
@RequiredArgsConstructor
@Tag(name = "Contract Review", description = "合同审查API")
public class ContractReviewController {
    private final ContractReviewService contractReviewService;
    private final ReportService reportService;
    private final ContractFeignClient  contractFeignClient;
    /**
     * 创建合同审查任务
     */
    @RemotePreAuthorize("@ss.hasAnyRoles('admin,common')")
    @PostMapping("/tasks")
    @Operation(summary = "创建审查任务", description = "创建新的合同审查任务")
    public ResponseEntity<ContractTaskDto> createReviewTask(@Valid @RequestBody ContractReviewCreateRequestDto requestDto) {

        ContractReview contractReview = contractReviewService.createContractReviewTask(requestDto);

        return ResponseEntity.ok(ContractTaskMapper.INSTANCE.toDto(contractReview));
    }

    /**
     * 更新合同审查任务
     */
    @RemotePreAuthorize("@ss.hasAnyRoles('admin,common')")
    @PutMapping("/tasks/{contractTaskId}")
    @Operation(summary = "更新审查任务", description = "更新合同审查任务的信息")
    public ResponseEntity<ContractTaskDto> updateReviewTask(@Valid @RequestBody ContractReviewRequestDto requestDto) {

        ContractReview contractReview = contractReviewService.updateContractReviewTask(requestDto);

        return ResponseEntity.ok(ContractTaskMapper.INSTANCE.toDto(contractReview));
    }

    /**
     * 删除合同审查任务
     */
    @RemotePreAuthorize("@ss.hasAnyRoles('admin,common')")
    @DeleteMapping("/tasks/{contractTaskId}")
    @Operation(summary = "删除审查任务", description = "删除指定的合同审查任务")
    public ResponseEntity<Boolean> deleteReviewTask(@PathVariable("contractTaskId") Long contractTaskId) {
        return ResponseEntity.ok(contractReviewService.deleteContractReviewTask(contractTaskId));
    }
    
    /**
     * 启动合同审查
     */
    @RemotePreAuthorize("@ss.hasAnyRoles('admin,common')")
    @PostMapping("/tasks/{taskId}/start")
    @Operation(summary = "启动审查", description = "启动指定的合同审查任务")
    public ResponseEntity<Void> startReview(@PathVariable("taskId") Long taskId) {
        TaskId id = TaskId.of(taskId);
        contractReviewService.startContractReview(id);
        return ResponseEntity.ok().build();
    }
    
    /**
     * 获取合同任务详情
     */
    @RemotePreAuthorize("@ss.hasAnyRoles('admin,common,guest')")
    @GetMapping("/tasks/{taskId}")
    @Operation(summary = "获取任务详情", description = "获取合同审查任务的详细信息")
    public ResponseEntity<ContractTaskDetailDto> getContractTask(@PathVariable("taskId") Long taskId) {
        TaskId id = TaskId.of(taskId);
        return ResponseEntity.ok(contractReviewService.getContractTaskDetailByTaskId(id));
    }

    /**
     * 根据合同ID获取任务列表
     */
    @RemotePreAuthorize("@ss.hasAnyRoles('admin,common,guest')")
    @GetMapping("/contracts/{contractId}/tasks")
    @Operation(summary = "获取合同任务", description = "获取指定合同的所有审查任务")
    public ResponseEntity<List<ContractTaskDto>> getTasksByContractId(
            @PathVariable("contractId") Long contractId) {

        List<ContractReview> reviews = contractReviewService.getTasksByContractId(contractId);
        List<ContractTaskDto> taskDtos = reviews.stream()
                .map(ContractTaskMapper.INSTANCE::toDto)
                .toList();

        return ResponseEntity.ok(taskDtos);
    }
    
    /**
     * 重试任务
     */
    @RemotePreAuthorize("@ss.hasAnyRoles('admin,common')")
    @PostMapping("/{taskId}/retry")
    @Operation(summary = "重试任务", description = "重试失败的任务")
    public ResponseEntity<Void> retryTask(@PathVariable("taskId") Long taskId) {
        TaskId id = TaskId.of(taskId);
        contractReviewService.retryTask(id);
        return ResponseEntity.ok().build();
    }

    /**
     * 获取合同任务列表（带统计信息）
     */
    @Anonymous
    @GetMapping("/tasks")
    @Operation(summary = "获取任务列表", description = "获取合同审查任务列表，包含统计信息和分页查询")
    public ResponseEntity<TaskListResponseDto> getTaskList(@Valid TaskListQueryRequestDto queryRequest) {
        TaskListResponseDto response = contractReviewService.getTaskList(queryRequest);
        return ResponseEntity.ok(response);
    }

    /**
     * 获取任务实时进度
     */
    @RemotePreAuthorize("@ss.hasAnyRoles('admin,common,guest')")
    @GetMapping("/tasks/{taskId}/progress")
    @Operation(summary = "获取任务进度", description = "获取任务的实时进度和预计剩余时间")
    public ResponseEntity<TaskProgressDto> getTaskProgress(@PathVariable("taskId") Long taskId) {
        TaskId id = TaskId.of(taskId);
        TaskProgressDto progress = contractReviewService.getTaskProgress(id);
        return ResponseEntity.ok(progress);
    }

    /**
     * 获取报告详情
     * 根据任务ID获取完整的审查报告信息
     */
    @RemotePreAuthorize("@ss.hasAnyRoles('admin,common,guest')")
    @GetMapping("/reports/{taskId}")
    @Operation(summary = "获取报告详情", description = "根据任务ID获取完整的审查报告信息")
    public ResponseEntity<ApiResponse<ReportDetailDto>> getReportDetail(@PathVariable("taskId") Long taskId) {
        TaskId id = TaskId.of(taskId);

        try {
            ReportDetailDto reportDetail = reportService.getReportDetail(id);
            return ResponseEntity.ok(ApiResponse.success(reportDetail));
        } catch (BusinessException e) {
            if ("DATA001".equals(e.getErrorCode())) {
                return ResponseEntity.status(404)
                        .body(ApiResponse.error(40401, "报告不存在"));
            }
            throw e;
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(40001, "无效的任务ID"));
        }
    }
}