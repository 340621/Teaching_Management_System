package com.example.student.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("course_material")
public class CourseMaterial {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long courseId;
    private String fileName;
    private String fileUrl;
    private String fileType;
    private Long fileSize;
    private String description;
    private Long uploaderId;
    private LocalDateTime uploadTime;
    private LocalDateTime updateTime;
    private Integer status;
}