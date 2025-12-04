package com.contractreview.reviewengine.interfaces.rest.controller;

import com.contractreview.reviewengine.application.service.ContractReviewService;
import com.contractreview.reviewengine.domain.model.ContractReview;
import com.contractreview.reviewengine.domain.model.ReviewResult;
import com.contractreview.reviewengine.domain.model.TaskId;
import com.contractreview.reviewengine.interfaces.rest.dto.ContractReviewCreateRequestDto;
import com.contractreview.reviewengine.interfaces.rest.dto.ContractReviewRequestDto;
import com.contractreview.reviewengine.interfaces.rest.dto.ContractTaskDto;
import com.contractreview.reviewengine.interfaces.rest.dto.ReviewResultDto;
import com.contractreview.reviewengine.interfaces.rest.mapper.ContractTaskMapper;
import com.contractreview.reviewengine.interfaces.rest.mapper.ReviewResultMapper;
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
import java.util.Optional;

/**
 * 合同审查REST控制器
 */
@RestController
@RequestMapping("/api/v1/contract-review")
@RequiredArgsConstructor
@Tag(name = "Contract Review", description = "合同审查API")
public class ContractReviewController {
    private final ContractReviewService contractReviewService;
    
    /**
     * 创建合同审查任务
     */
    @PostMapping("/tasks")
    @Operation(summary = "创建审查任务", description = "创建新的合同审查任务")
    public ResponseEntity<ContractTaskDto> createReviewTask(@Valid @RequestBody ContractReviewCreateRequestDto requestDto) {

        ContractReview contractReview = contractReviewService.createContractReviewTask(requestDto);

        return ResponseEntity.ok(ContractTaskMapper.INSTANCE.toDto(contractReview));
    }

    /**
     * 更新合同审查任务
     */
    @PutMapping("/tasks/{contractTaskId}")
    @Operation(summary = "更新审查任务", description = "更新合同审查任务的信息")
    public ResponseEntity<ContractTaskDto> updateReviewTask(@Valid @RequestBody ContractReviewRequestDto requestDto) {

        ContractReview contractReview = contractReviewService.updateContractReviewTask(requestDto);

        return ResponseEntity.ok(ContractTaskMapper.INSTANCE.toDto(contractReview));
    }

    /**
     * 删除合同审查任务
     */
    @DeleteMapping("/tasks/{contractTaskId}")
    @Operation(summary = "删除审查任务", description = "删除指定的合同审查任务")
    public ResponseEntity<Boolean> deleteReviewTask(@PathVariable("contractTaskId") Long contractTaskId) {
        return ResponseEntity.ok(contractReviewService.deleteContractReviewTask(contractTaskId));
    }
    
    /**
     * 启动合同审查
     */
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
    @GetMapping("/tasks/{taskId}")
    @Operation(summary = "获取任务详情", description = "获取合同审查任务的详细信息")
    public ResponseEntity<ContractTaskDto> getContractTask(@PathVariable("taskId") Long taskId) {
        TaskId id = TaskId.of(taskId);
        ContractReview contractReview = contractReviewService.getContractTask(id);
        return ResponseEntity.ok(ContractTaskMapper.INSTANCE.toDto(contractReview));
    }

    /**
     * 严格获取合同任务详情（通过taskId）
     *
     * 该方法严格验证taskId与contract_task之间的一一对应关系。
     * 如果task存在但对应的contract_task不存在，将抛出BusinessException。
     */
    @GetMapping("/tasks/{taskId}/strict")
    @Operation(summary = "严格获取任务详情", description = "通过taskId严格获取对应的合同审查任务，验证一一对应关系")
    public ResponseEntity<ContractTaskDto> getContractTaskByTaskId(@PathVariable("taskId") Long taskId) {
        TaskId id = TaskId.of(taskId);
        ContractReview contractReview = contractReviewService.getContractTaskByTaskId(id);
        return ResponseEntity.ok(ContractTaskMapper.INSTANCE.toDto(contractReview));
    }
    
    /**
     * 获取审查结果
     */
    @GetMapping("/tasks/{taskId}/result")
    @Operation(summary = "获取审查结果", description = "获取合同审查的结果")
    public ResponseEntity<ReviewResultDto> getReviewResult(@PathVariable("taskId") Long taskId) {
        TaskId id = TaskId.of(taskId);
        Optional<ReviewResult> result = contractReviewService.getReviewResult(id);
        
        if (result.isPresent()) {
            return ResponseEntity.ok(ReviewResultMapper.INSTANCE.toDto(result.get()));
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * 根据合同ID获取任务列表
     */
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
     * 获取合同的最新任务
     */
    @GetMapping("/contracts/{contractId}/latest-task")
    @Operation(summary = "获取最新任务", description = "获取指定合同的最新审查任务")
    public ResponseEntity<ContractTaskDto> getLatestTaskByContractId(
            @PathVariable("contractId") Long contractId) {

        Optional<ContractReview> latestReview = contractReviewService.getLatestTaskByContractId(contractId);

        if (latestReview.isPresent()) {
            return ResponseEntity.ok(ContractTaskMapper.INSTANCE.toDto(latestReview.get()));
        } else {
            return ResponseEntity.notFound().build();
        }
    }


    /**
     * 重试任务
     */
    @PostMapping("/{taskId}/retry")
    @Operation(summary = "重试任务", description = "重试失败的任务")
    public ResponseEntity<Void> retryTask(@PathVariable("taskId") Long taskId) {
        TaskId id = TaskId.of(taskId);
        contractReviewService.retryTask(id);
        return ResponseEntity.ok().build();
    }
}