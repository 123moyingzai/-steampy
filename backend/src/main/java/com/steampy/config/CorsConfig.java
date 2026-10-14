package com.steampy.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * 全局跨域配置
 * 核心职责：放行所有来源的 HTTP 请求（开发阶段使用），覆盖所有路径 /**
 *
 * @author steampy
 * @date 2026-10-14
 */
@Configuration
public class CorsConfig {

    /**
     * 构造 CorsFilter Bean，允许任意来源、任意请求头、任意 HTTP 方法跨域访问
     * 同时支持携带 Cookie（allowCredentials=true），预检请求缓存 1 小时
     *
     * @return 全局 CorsFilter 实例
     */
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOriginPattern("*");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
