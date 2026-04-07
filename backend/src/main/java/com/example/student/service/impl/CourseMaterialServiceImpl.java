package com.example.student.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.student.common.PageResult;
import com.example.student.entity.CourseMaterial;
import com.example.student.mapper.CourseMaterialMapper;
import com.example.student.service.CourseMaterialService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * 课程资料服务实现
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CourseMaterialServiceImpl implements CourseMaterialService {

    private final CourseMaterialMapper courseMaterialMapper;

    @Value("${file.upload.path:/uploads}")
    private String uploadPath;

    @Override
    public PageResult<CourseMaterial> getCourseMaterials(Long courseId, Integer pageNum, Integer pageSize) {
        Page<CourseMaterial> page = new Page<>(pageNum, pageSize);
        IPage<CourseMaterial> result = courseMaterialMapper.selectCourseMaterials(page, courseId);
        return PageResult.build(result.getRecords(), result.getTotal(), pageNum, pageSize);
    }

    @Override
    public boolean uploadCourseMaterial(Long courseId, String name, String description, String type, MultipartFile file) {
        log.info("开始上传课程资料: courseId={}, name={}, description={}, type={}, fileSize={}", courseId, name, description, type, file.getSize());
        try {
            // 确保上传目录存在
            Path uploadDir = Paths.get(uploadPath);
            log.info("上传目录: {}", uploadDir);
            if (!Files.exists(uploadDir)) {
                log.info("上传目录不存在，开始创建");
                Files.createDirectories(uploadDir);
                log.info("上传目录创建成功");
            }

            // 生成唯一文件名，保持原始文件名
            String originalFilename = file.getOriginalFilename();
            log.info("原始文件名: {}", originalFilename);
            String extension = originalFilename != null ? originalFilename.substring(originalFilename.lastIndexOf('.')) : "";
            String fileName = UUID.randomUUID().toString() + "_" + originalFilename;
            Path filePath = uploadDir.resolve(fileName);
            log.info("保存文件路径: {}", filePath);

            // 保存文件
            file.transferTo(filePath);
            log.info("文件保存成功");

            // 保存数据库记录
            CourseMaterial material = new CourseMaterial();
            material.setCourseId(courseId);
            material.setFileName(name); // 保存原始文件名
            material.setDescription(description); // 保存文件描述
            material.setFileType(type);
            material.setFileUrl(filePath.toString());
            material.setFileSize(file.getSize());

            int result = courseMaterialMapper.insert(material);
            log.info("数据库插入结果: {}", result);
            return result > 0;
        } catch (IOException e) {
            log.error("上传课程资料失败", e);
            return false;
        } catch (Exception e) {
            log.error("上传课程资料发生未知错误", e);
            return false;
        }
    }

    @Override
    public boolean deleteCourseMaterial(Long id) {
        try {
            // 获取资料信息
            CourseMaterial material = courseMaterialMapper.selectById(id);
            if (material != null) {
                // 删除文件
                Path filePath = Paths.get(material.getFileUrl());
                if (Files.exists(filePath)) {
                    Files.delete(filePath);
                }
                // 删除数据库记录
                return courseMaterialMapper.deleteById(id) > 0;
            }
            return false;
        } catch (IOException e) {
            log.error("删除课程资料失败", e);
            return false;
        }
    }

    @Override
    public void downloadCourseMaterial(Long id, HttpServletResponse response) throws IOException {
        // 获取资料信息
        CourseMaterial material = courseMaterialMapper.selectById(id);
        if (material == null) {
            throw new IOException("资料不存在");
        }

        // 读取文件
        Path filePath = Paths.get(material.getFileUrl());
        if (!Files.exists(filePath)) {
            throw new IOException("文件不存在");
        }

        // 设置响应头
        response.setContentType("application/octet-stream");
        response.setContentLengthLong(material.getFileSize());
        // 使用filename*参数，支持UTF-8编码的文件名
        String encodedFileName = java.net.URLEncoder.encode(material.getFileName(), "UTF-8")
                .replace("+", "%20");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFileName);

        // 输出文件
        try (OutputStream out = response.getOutputStream()) {
            Files.copy(filePath, out);
        }
    }
}
