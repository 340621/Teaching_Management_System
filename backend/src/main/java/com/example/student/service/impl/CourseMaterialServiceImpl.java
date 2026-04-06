package com.example.student.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.student.entity.CourseMaterial;
import com.example.student.mapper.CourseMaterialMapper;
import com.example.student.service.CourseMaterialService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class CourseMaterialServiceImpl extends ServiceImpl<CourseMaterialMapper, CourseMaterial> implements CourseMaterialService {

    private static final String UPLOAD_DIR = "D:/桌面/LLM_Teaching_Management_System/backend/uploads/course_materials/";

    @Override
    public CourseMaterial uploadMaterial(Long courseId, Long uploaderId, MultipartFile file, String description) {
        // 确保上传目录存在
        File uploadDir = new File(UPLOAD_DIR);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        // 生成唯一文件名
        String originalFileName = file.getOriginalFilename();
        String fileExtension = originalFileName.substring(originalFileName.lastIndexOf('.'));
        String fileName = UUID.randomUUID().toString() + fileExtension;
        String filePath = UPLOAD_DIR + fileName;

        try {
            // 保存文件到本地
            file.transferTo(new File(filePath));

            // 创建课程资料记录
            CourseMaterial material = new CourseMaterial();
            material.setCourseId(courseId);
            material.setFileName(originalFileName);
            material.setFileUrl(filePath);
            material.setFileType(file.getContentType());
            material.setFileSize(file.getSize());
            material.setDescription(description);
            material.setUploaderId(uploaderId);
            material.setUploadTime(LocalDateTime.now());
            material.setUpdateTime(LocalDateTime.now());
            material.setStatus(1);

            // 保存到数据库
            save(material);
            return material;
        } catch (IOException e) {
            throw new RuntimeException("文件上传失败", e);
        }
    }

    @Override
    public List<CourseMaterial> getMaterialsByCourseId(Long courseId) {
        return lambdaQuery().eq(CourseMaterial::getCourseId, courseId).eq(CourseMaterial::getStatus, 1).list();
    }

    @Override
    public boolean deleteMaterial(Long id, Long uploaderId) {
        CourseMaterial material = getById(id);
        if (material == null || !material.getUploaderId().equals(uploaderId)) {
            return false;
        }

        // 删除本地文件
        File file = new File(material.getFileUrl());
        if (file.exists()) {
            file.delete();
        }

        // 更新状态为已删除
        material.setStatus(0);
        material.setUpdateTime(LocalDateTime.now());
        return updateById(material);
    }
}