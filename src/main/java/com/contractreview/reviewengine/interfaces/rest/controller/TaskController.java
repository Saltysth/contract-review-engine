package com.contractreview.reviewengine.interfaces.rest.controller;

import com.contractreview.reviewengine.application.service.TaskService;
import com.contractreview.reviewengine.domain.enums.TaskStatus;
import com.contractreview.reviewengine.domain.model.Task;
import com.contractreview.reviewengine.domain.model.TaskId;
import com.contractreview.reviewengine.interfaces.rest.dto.TaskDto;
import com.contractreview.reviewengine.interfaces.rest.dto.TaskStatisticsDto;
import com.contractreview.reviewengine.interfaces.rest.mapper.TaskMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
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
            @RequestParam(required = false) TaskStatus status,
            Pageable pageable) {
        
        Page<Task> tasks;
        if (status != null) {
            // 转换为domain model类型
            com.contractreview.reviewengine.domain.enums.TaskStatus domainStatus =
                com.contractreview.reviewengine.domain.enums.TaskStatus.valueOf(status.name());
            tasks = taskService.getTasksByStatus(domainStatus, pageable);
        } else {
            // TODO: 实现获取所有任务的分页方法
            com.contractreview.reviewengine.domain.enums.TaskStatus domainStatus =
                com.contractreview.reviewengine.domain.enums.TaskStatus.valueOf(TaskStatus.PENDING.name());
            tasks = taskService.getTasksByStatus(domainStatus, pageable);
        }
        
        Page<TaskDto> taskDtos = tasks.map(TaskMapper.INSTANCE::toDto);
        return ResponseEntity.ok(taskDtos);
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
     * 重试任务
     */
    @PostMapping("/{taskId}/retry")
    @Operation(summary = "重试任务", description = "重试失败的任务")
    public ResponseEntity<Void> retryTask(@PathVariable Long taskId) {
        TaskId id = TaskId.of(taskId);
        taskService.retryTask(id);
        return ResponseEntity.ok().build();
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
}