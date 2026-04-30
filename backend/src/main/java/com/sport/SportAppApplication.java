package com.sport;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 运动App后端启动类
 */
@SpringBootApplication
@MapperScan("com.sport.mapper")
public class SportAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(SportAppApplication.class, args);
        System.out.println("=========================================");
        System.out.println("运动App后端服务启动成功！");
        System.out.println("API文档地址: http://localhost:8080/api/v1/doc.html");
        System.out.println("=========================================");
    }
}
