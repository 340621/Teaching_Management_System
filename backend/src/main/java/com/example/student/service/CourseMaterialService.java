package com.example.student.service;

import com.example.student.common.PageResult;
import com.example.student.entity.CourseMaterial;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 课程资料服务接口
 */
public interface CourseMaterialService {

    /**
     * 获取课程资料列表
     *
     * @param courseId 课程ID
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    PageResult<CourseMaterial> getCourseMaterials(Long courseId, Integer pageNum, Integer pageSize);

    /**
     * 上传课程资料
     * @param courseId 课程ID
     * @param name 资料名称
     * @param description 资料描述
     * @param type 资料类型
     * @param file 资料文件
     * @return 是否上传成功
     */
    boolean uploadCourseMaterial(Long courseId, String name, String description, String type, MultipartFile file);

    /**
     * 删除课程资料
     *
     * @param id 资料ID
     * @return 删除结果
     */
    boolean deleteCourseMaterial(Long id);

    /**
     * 下载课程资料
     *
     * @param id 资料ID
     * @param response HTTP响应
     * @throws IOException IO异常
     */
    void downloadCourseMaterial(Long id, HttpServletResponse response) throws IOException;
}
