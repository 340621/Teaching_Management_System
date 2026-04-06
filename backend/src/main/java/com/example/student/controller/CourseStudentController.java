package com.example.student.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.student.common.Result;
import com.example.student.dto.StudentQueryDTO;
import com.example.student.entity.CourseSelection;
import com.example.student.entity.Student;
import com.example.student.service.CourseSelectionService;
import com.example.student.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/course/students")
public class CourseStudentController {

    @Autowired
    private CourseSelectionService courseSelectionService;

    @Autowired
    private StudentService studentService;

    @GetMapping("/list/{courseId}")
    public Result<?> getCourseStudents(
            @PathVariable Long courseId,
            StudentQueryDTO queryDTO) {
        try {
            // 获取课程的学生列表
            Page<Map<String, Object>> page = courseSelectionService.getStudentsByCourseId(
                    courseId, 
                    queryDTO.getPageNum(), 
                    queryDTO.getPageSize(),
                    queryDTO.getName(),
                    queryDTO.getStudentId()
            );
            return Result.success(page);
        } catch (Exception e) {
            return Result.error("获取学生列表失败: " + e.getMessage());
        }
    }
}