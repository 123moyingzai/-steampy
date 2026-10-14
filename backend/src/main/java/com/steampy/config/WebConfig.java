package com.steampy.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

/**
 * Web MVC 资源映射配置
 * 核心职责：将本地文件系统中的上传目录映射为可通过 HTTP 访问的 /uploads/** 路径
 *
 * @author steampy
 * @date 2026-10-14
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /** 上传目录相对路径，可通过 app.upload-dir 配置覆盖，默认 uploads */
    @Value("${app.upload-dir:uploads}")
    private String uploadDir;

    /**
     * 注册静态资源处理器：将 /uploads/** 请求映射到本地文件系统的上传目录
     *
     * @param registry Spring 提供的资源处理器注册表
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String abs = new File(uploadDir).getAbsolutePath();
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + abs + File.separator);
    }
}
