package com.example.student.controller;

import com.example.student.common.Result;
import com.example.student.service.CourseSelectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 系统配置控制器
 */
@RestController
@RequestMapping("/api/system/config")
@RequiredArgsConstructor
public class SystemConfigController {

    private final CourseSelectionService courseSelectionService;

    /**
     * 获取选课时间配置
     */
    @GetMapping("/selection")
    public Result<Map<String, Object>> getSelectionSettings() {
        Map<String, Object> settings = courseSelectionService.getSelectionSettings();
        return Result.success(settings);
    }

    /**
     * 更新选课时间配置
     */
    @PutMapping("/selection")
    public Result<Boolean> updateSelectionSettings(@RequestBody Map<String, Object> settings) {
        boolean success = courseSelectionService.updateSelectionSettings(settings);
        return Result.success(success);
    }
}
