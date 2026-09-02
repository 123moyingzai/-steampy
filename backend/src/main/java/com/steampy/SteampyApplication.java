package com.steampy;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.steampy.mapper")
public class SteampyApplication {
    public static void main(String[] args) {
        SpringApplication.run(SteampyApplication.class, args);
        System.out.println("🚀 SteamPY 后端启动成功! http://localhost:8080");
    }
}
