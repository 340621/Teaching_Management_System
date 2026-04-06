package com.example.student.controller;

import com.example.student.common.Result;
import com.example.student.entity.CourseMaterial;
import com.example.student.service.CourseMaterialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.List;

@RestController
@RequestMapping("/course/material")
public class CourseMaterialController {

    @Autowired
    private CourseMaterialService courseMaterialService;

    @PostMapping("/upload")
    public Result<?> uploadMaterial(
            @RequestParam Long courseId,
            @RequestParam Long uploaderId,
            @RequestParam MultipartFile file,
            @RequestParam(required = false) String description) {
        try {
            CourseMaterial material = courseMaterialService.uploadMaterial(courseId, uploaderId, file, description);
            return Result.success(material);
        } catch (Exception e) {
            return Result.error("文件上传失败: " + e.getMessage());
        }
    }

    @GetMapping("/list/{courseId}")
    public Result<?> getMaterialsByCourseId(@PathVariable Long courseId) {
        List<CourseMaterial> materials = courseMaterialService.getMaterialsByCourseId(courseId);
        return Result.success(materials);
    }

    @DeleteMapping("/delete/{id}")
    public Result<?> deleteMaterial(@PathVariable Long id, @RequestParam Long uploaderId) {
        boolean success = courseMaterialService.deleteMaterial(id, uploaderId);
        if (success) {
            return Result.success("删除成功");
        } else {
            return Result.error("删除失败，权限不足或文件不存在");
        }
    }

    @GetMapping("/download/{id}")
    public void downloadMaterial(@PathVariable Long id, HttpServletResponse response) throws IOException {
        CourseMaterial material = courseMaterialService.getById(id);
        if (material == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        File file = new File(material.getFileUrl());
        if (!file.exists()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        response.setContentType(material.getFileType());
        response.setHeader("Content-Disposition", "attachment; filename=" + material.getFileName());
        response.setContentLengthLong(material.getFileSize());

        try (OutputStream outputStream = response.getOutputStream()) {
            Files.copy(file.toPath(), outputStream);
        }
    }
}