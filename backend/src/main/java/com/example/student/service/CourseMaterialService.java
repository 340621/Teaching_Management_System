package com.example.student.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.student.entity.CourseMaterial;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface CourseMaterialService extends IService<CourseMaterial> {
    CourseMaterial uploadMaterial(Long courseId, Long uploaderId, MultipartFile file, String description);
    List<CourseMaterial> getMaterialsByCourseId(Long courseId);
    boolean deleteMaterial(Long id, Long uploaderId);
}