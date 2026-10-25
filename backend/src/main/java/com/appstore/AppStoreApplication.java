package com.appstore;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 轻量应用商店系统启动类
 */
@SpringBootApplication
@MapperScan("com.appstore.mapper")
public class AppStoreApplication {
    public static void main(String[] args) {
        SpringApplication.run(AppStoreApplication.class, args);
        System.out.println("========================================");
        System.out.println("  轻量应用商店系统后端启动成功！");
        System.out.println("  访问地址: http://localhost:8080/api");
        System.out.println("========================================");
    }
}
