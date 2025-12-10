package com.contractreview.reviewengine.interfaces.rest.controller;

import com.contractreview.reviewengine.application.service.ContractReviewService;
import com.contractreview.reviewengine.application.service.TaskService;
import com.contractreview.reviewengine.domain.enums.TaskStatus;
import com.contractreview.reviewengine.domain.enums.TaskType;
import com.contractreview.reviewengine.domain.model.Task;
import com.contractreview.reviewengine.domain.model.TaskId;
import com.contractreview.reviewengine.interfaces.rest.dto.TaskDto;
import com.contractreview.reviewengine.interfaces.rest.dto.TaskStatisticsDto;
import com.contractreview.reviewengine.interfaces.rest.mapper.TaskMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 任务管理REST控制器
 */
@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
@Tag(name = "Task Management", description = "任务管理API")
public class TaskController {

    private final TaskService taskService;
    private final ContractReviewService contractReviewService;

    /**
     * 获取任务详情
     */
    @GetMapping("/{taskId}")
    @Operation(summary = "获取任务详情", description = "根据任务ID获取任务详细信息")
    public ResponseEntity<TaskDto> getTask(@PathVariable Long taskId) {
        TaskId id = TaskId.of(taskId);
        Task task = taskService.getTaskById(id);
        return ResponseEntity.ok(TaskMapper.INSTANCE.toDto(task));
    }

    /**
     * 分页获取任务列表
     */
    @GetMapping
    @Operation(summary = "获取任务列表", description = "分页获取任务列表，可按状态筛选")
    public ResponseEntity<Page<TaskDto>> getTasks(
            @Parameter(description = "任务状态筛选")
            @RequestParam(required = false) TaskStatus status,
            @Parameter(description = "页码，从0开始")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "排序字段")
            @RequestParam(defaultValue = "createdTime") String sort,
            @Parameter(description = "排序方向")
            @RequestParam(defaultValue = "desc") String direction) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(direction), sort));
        Page<Task> tasks;

        if (status != null) {
            tasks = taskService.getTasksByStatus(status, pageable);
        } else {
            // 获取所有任务并手动分页
            List<Task> allTasks = taskService.getAllTasks();
            List<TaskDto> allTaskDtos = allTasks.stream()
                    .map(TaskMapper.INSTANCE::toDto)
                    .collect(java.util.stream.Collectors.toList());

            // 手动分页
            int start = page * size;
            int end = Math.min(start + size, allTaskDtos.size());
            List<TaskDto> paginatedDtos = allTaskDtos.subList(start, end);

            Page<TaskDto> pageResult = new org.springframework.data.domain.PageImpl<>(
                    paginatedDtos,
                    PageRequest.of(page, size),
                    allTaskDtos.size()
            );
            return ResponseEntity.ok(pageResult);
        }

        Page<TaskDto> taskDtos = tasks.map(TaskMapper.INSTANCE::toDto);
        return ResponseEntity.ok(taskDtos);
    }

    /**
     * 创建新任务
     */
    @PostMapping
    @Operation(summary = "创建新任务", description = "创建新的任务")
    public ResponseEntity<TaskDto> createTask(
            @Parameter(description = "任务名称")
            @RequestParam String taskName,
            @Parameter(description = "任务类型")
            @RequestParam String taskType,
            @Parameter(description = "创建用户ID")
            @RequestParam Long createdBy) {

        TaskType type = TaskType.valueOf(taskType.toUpperCase());
        Task task = taskService.createTask(taskName, type, createdBy);
        return ResponseEntity.ok(TaskMapper.INSTANCE.toDto(task));
    }

    /**
     * 启动任务
     */
    @PostMapping("/{taskId}/start")
    @Operation(summary = "启动任务", description = "启动指定的任务")
    public ResponseEntity<Void> startTask(@PathVariable Long taskId) {
        TaskId id = TaskId.of(taskId);
        taskService.startTask(id);
        return ResponseEntity.ok().build();
    }

    /**
     * 完成任务
     */
    @PostMapping("/{taskId}/complete")
    @Operation(summary = "完成任务", description = "完成指定的任务")
    public ResponseEntity<Void> completeTask(@PathVariable Long taskId) {
        TaskId id = TaskId.of(taskId);
        taskService.completeTask(id);
        return ResponseEntity.ok().build();
    }

    /**
     * 取消任务
     */
    @PostMapping("/{taskId}/cancel")
    @Operation(summary = "取消任务", description = "取消指定的任务")
    public ResponseEntity<Void> cancelTask(@PathVariable Long taskId) {
        TaskId id = TaskId.of(taskId);
        taskService.cancelTask(id);
        return ResponseEntity.ok().build();
    }

    /**
     * 删除任务
     */
    @DeleteMapping("/{taskId}")
    @Operation(summary = "删除任务", description = "删除指定的任务")
    public ResponseEntity<Void> deleteTask(@PathVariable("taskId") Long taskId) {
        TaskId id = TaskId.of(taskId);
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 获取任务统计信息
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取任务统计", description = "获取各状态任务的统计信息")
    public ResponseEntity<TaskStatisticsDto> getTaskStatistics() {
        List<Object[]> statistics = taskService.getTaskStatistics();
        TaskStatisticsDto dto = TaskStatisticsDto.fromStatistics(statistics);
        return ResponseEntity.ok(dto);
    }

    /**
     * 获取超时任务
     */
    @GetMapping("/timeout")
    @Operation(summary = "获取超时任务", description = "获取运行超时的任务列表")
    public ResponseEntity<List<TaskDto>> getTimeoutTasks() {
        List<Task> timeoutTasks = taskService.findTimeoutTasks();
        List<TaskDto> taskDtos = timeoutTasks.stream()
                .map(TaskMapper.INSTANCE::toDto)
                .toList();
        return ResponseEntity.ok(taskDtos);
    }

    /**
     * 获取可重试任务
     */
    @GetMapping("/retryable")
    @Operation(summary = "获取可重试任务", description = "获取可以重试的失败任务列表")
    public ResponseEntity<List<TaskDto>> getRetryableTasks() {
        List<Task> retryableTasks = taskService.findRetryableTasks();
        List<TaskDto> taskDtos = retryableTasks.stream()
                .map(TaskMapper.INSTANCE::toDto)
                .toList();
        return ResponseEntity.ok(taskDtos);
    }

    /**
     * 根据类型获取任务
     */
    @GetMapping("/type/{taskType}")
    @Operation(summary = "根据类型获取任务", description = "根据任务类型获取任务列表")
    public ResponseEntity<List<TaskDto>> getTasksByType(@PathVariable String taskType) {
        TaskType type = TaskType.valueOf(taskType.toUpperCase());
        List<Task> tasks = taskService.getTasksByType(type);
        List<TaskDto> taskDtos = tasks.stream()
                .map(TaskMapper.INSTANCE::toDto)
                .toList();
        return ResponseEntity.ok(taskDtos);
    }

    /**
     * 检查和处理超时任务
     */
    @PostMapping("/timeout/check")
    @Operation(summary = "检查超时任务", description = "检查并处理所有超时任务")
    public ResponseEntity<Void> checkAndHandleTimeoutTasks() {
        taskService.checkAndHandleTimeoutTasks();
        return ResponseEntity.ok().build();
    }

    /**
     * 获取所有任务
     */
    @GetMapping("/all")
    @Operation(summary = "获取所有任务", description = "获取所有任务列表")
    public ResponseEntity<List<TaskDto>> getAllTasks() {
        List<Task> tasks = taskService.getAllTasks();
        List<TaskDto> taskDtos = tasks.stream()
                .map(TaskMapper.INSTANCE::toDto)
                .toList();
        return ResponseEntity.ok(taskDtos);
    }

    /**
     * 获取任务状态显示名称列表
     */
    @GetMapping("/statuses")
    @Operation(summary = "获取任务状态列表", description = "获取所有任务状态的显示名称")
    public ResponseEntity<List<String>> getTaskStatuses() {
        List<String> displayNames = java.util.Arrays.stream(TaskStatus.values())
                .map(TaskStatus::getDisplayName)
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(displayNames);
    }

    /**
     * 检查任务名称是否存在
     */
    @GetMapping("/check-name")
    @Operation(summary = "检查任务名称", description = "检查指定的任务名称是否已存在")
    public ResponseEntity<Boolean> checkTaskNameExists(
            @Parameter(description = "任务名称", required = true)
            @RequestParam("taskName") String taskName) {
        boolean exists = taskService.existsByTaskName(taskName);
        return ResponseEntity.ok(exists);
    }
}