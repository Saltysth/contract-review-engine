package com.contractreview.reviewengine.interfaces.rest;

import com.contractreview.reviewengine.application.service.ContractReviewService;
import com.contractreview.reviewengine.domain.model.ContractTask;
import com.contractreview.reviewengine.domain.model.ReviewResult;
import com.contractreview.reviewengine.domain.model.TaskId;
import com.contractreview.reviewengine.interfaces.rest.dto.ContractReviewRequestDto;
import com.contractreview.reviewengine.interfaces.rest.dto.ContractTaskDto;
import com.contractreview.reviewengine.interfaces.rest.dto.ReviewResultDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
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
    public ResponseEntity<ContractTaskDto> createReviewTask(
            @Valid @RequestBody ContractReviewRequestDto request) {
        
        ContractTask contractTask = contractReviewService.createContractReviewTask(
                request.getContractId(),
                request.getFilePath(),
                request.getFileHash(),
                request.getReviewType(),
                request.toTaskConfiguration(),
                request.getMetadata()
        );
        
        return ResponseEntity.ok(ContractTaskDto.fromDomain(contractTask));
    }
    
    /**
     * 启动合同审查
     */
    @PostMapping("/tasks/{taskId}/start")
    @Operation(summary = "启动审查", description = "启动指定的合同审查任务")
    public ResponseEntity<Void> startReview(@PathVariable Long taskId) {
        TaskId id = TaskId.of(taskId);
        contractReviewService.startContractReview(id);
        return ResponseEntity.ok().build();
    }
    
    /**
     * 获取合同任务详情
     */
    @GetMapping("/tasks/{taskId}")
    @Operation(summary = "获取任务详情", description = "获取合同审查任务的详细信息")
    public ResponseEntity<ContractTaskDto> getContractTask(@PathVariable Long taskId) {
        TaskId id = TaskId.of(taskId);
        ContractTask contractTask = contractReviewService.getContractTask(id);
        return ResponseEntity.ok(ContractTaskDto.fromDomain(contractTask));
    }
    
    /**
     * 获取审查结果
     */
    @GetMapping("/tasks/{taskId}/result")
    @Operation(summary = "获取审查结果", description = "获取合同审查的结果")
    public ResponseEntity<ReviewResultDto> getReviewResult(@PathVariable Long taskId) {
        TaskId id = TaskId.of(taskId);
        Optional<ReviewResult> result = contractReviewService.getReviewResult(id);
        
        if (result.isPresent()) {
            return ResponseEntity.ok(ReviewResultDto.fromDomain(result.get()));
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
            @PathVariable String contractId) {
        
        List<ContractTask> tasks = contractReviewService.getTasksByContractId(contractId);
        List<ContractTaskDto> taskDtos = tasks.stream()
                .map(ContractTaskDto::fromDomain)
                .toList();
        
        return ResponseEntity.ok(taskDtos);
    }
    
    /**
     * 获取合同的最新任务
     */
    @GetMapping("/contracts/{contractId}/latest-task")
    @Operation(summary = "获取最新任务", description = "获取指定合同的最新审查任务")
    public ResponseEntity<ContractTaskDto> getLatestTaskByContractId(
            @PathVariable String contractId) {
        
        Optional<ContractTask> latestTask = contractReviewService.getLatestTaskByContractId(contractId);
        
        if (latestTask.isPresent()) {
            return ResponseEntity.ok(ContractTaskDto.fromDomain(latestTask.get()));
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}