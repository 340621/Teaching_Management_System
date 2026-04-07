package com.example.student.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.student.entity.CourseMaterial;

/**
 * 课程资料Mapper
 */
public interface CourseMaterialMapper extends BaseMapper<CourseMaterial> {

    /**
     * 分页查询课程资料
     *
     * @param page 分页参数
     * @param courseId 课程ID
     * @return 分页结果
     */
    IPage<CourseMaterial> selectCourseMaterials(Page<CourseMaterial> page, Long courseId);
}
