package com.steampy;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * SteamPY 后端应用启动类
 * 核心职责：作为 Spring Boot 应用入口，扫描 Mapper、开启定时任务调度
 *
 * @author steampy
 * @date 2026-10-14
 */
@SpringBootApplication
@EnableScheduling
@MapperScan("com.steampy.mapper")
public class SteampyApplication {

    /**
     * Spring Boot 启动入口
     * 启动内嵌 Tomcat 并加载全部配置与 Bean
     *
     * @param args 命令行启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(SteampyApplication.class, args);
        System.out.println("🚀 SteamPY 后端启动成功! http://localhost:8080");
    }
}
