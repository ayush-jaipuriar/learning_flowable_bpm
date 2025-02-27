package com.example.loanapplication.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.TaskService;
import org.flowable.task.api.Task;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Slf4j
public class TaskController {

    private final TaskService taskService;

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getTasks(@RequestParam(required = false) String assignee) {
        List<Task> tasks;
        if (assignee != null && !assignee.isEmpty()) {
            tasks = taskService.createTaskQuery().taskAssignee(assignee).list();
        } else {
            tasks = taskService.createTaskQuery().taskCandidateGroup("loan-officers").list();
        }
        
        List<Map<String, Object>> result = tasks.stream()
                .map(task -> {
                    Map<String, Object> taskMap = new HashMap<>();
                    taskMap.put("id", task.getId());
                    taskMap.put("name", task.getName());
                    taskMap.put("description", task.getDescription());
                    taskMap.put("createTime", task.getCreateTime());
                    taskMap.put("assignee", task.getAssignee());
                    taskMap.put("formKey", task.getFormKey());
                    return taskMap;
                })
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/{taskId}/variables")
    public ResponseEntity<Map<String, Object>> getTaskVariables(@PathVariable String taskId) {
        Map<String, Object> variables = taskService.getVariables(taskId);
        return ResponseEntity.ok(variables);
    }
    
    @PostMapping("/{taskId}/claim")
    public ResponseEntity<Void> claimTask(@PathVariable String taskId, @RequestParam String userId) {
        taskService.claim(taskId, userId);
        log.info("Task {} claimed by user {}", taskId, userId);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/{taskId}/complete")
    public ResponseEntity<Void> completeTask(
            @PathVariable String taskId,
            @RequestBody Map<String, Object> variables) {
        
        taskService.complete(taskId, variables);
        log.info("Task {} completed with variables: {}", taskId, variables);
        return ResponseEntity.ok().build();
    }
}
