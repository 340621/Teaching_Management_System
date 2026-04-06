package com.example.student;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 教学管理系统启动类
 *
 * @author example
 */
@SpringBootApplication
@EnableTransactionManagement
@MapperScan("com.example.student.mapper")
public class LLM_Teaching_Management_SystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(LLM_Teaching_Management_SystemApplication.class, args);
    }

} 