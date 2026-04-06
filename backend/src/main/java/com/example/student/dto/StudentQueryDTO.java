package com.example.student.dto;

import lombok.Data;

@Data
public class StudentQueryDTO {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    private String name;
    private String studentId;
    private String studentNo;
    private Long classId;
    private Integer gender;
    private Long majorId;
    private Long departmentId;
    private String phone;
    private Integer status;
    private String startDate;
    private String endDate;
}