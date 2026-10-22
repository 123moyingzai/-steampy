package com.steampy.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Web MVC 资源映射配置 + SPA history 路由 fallback 过滤器。
 *
 * 职责：
 *   1. 将本地文件系统中的上传目录映射为 /uploads/**（后端文件服务）
 *   2. 单 jar 部署时，非 /api/** / /uploads/** / /assets/** / 已映射静态资源 的请求，
 *      若路径不含文件扩展名或扩展名是 SPA 资源白名单以外的，则 forward 到 /index.html，
 *      由 Vue Router 在前端解析真正的子路由（刷新 /admin/xxx、/game/xxx 不再 404）。
 *      开发态走 Vite 代理，该 fallback 也无害——Spring Boot 在开发后端时不托管前端。
 *
 * @author steampy
 * @date 2026-10-14
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /** 上传目录相对路径，可通过 app.upload-dir 配置覆盖，默认 uploads */
    @Value("${app.upload-dir:uploads}")
    private String uploadDir;

    @Value("${steampy.spa-fallback:true}")
    private boolean spaFallback;

    /**
     * 注册静态资源处理器：将 /uploads/** 请求映射到本地文件系统的上传目录
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String abs = new File(uploadDir).getAbsolutePath();
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + abs + File.separator);
    }

    /**
     * SPA fallback 过滤器。
     *
     * 匹配规则：
     *   - 跳过 /api/**、/uploads/**、/assets/**、/favicon.ico 等明确归属的路径
     *   - 跳过已有扩展名的静态资源（.js / .css / .png / .svg / .json / .ico / .woff2 …）
     *   - 其余请求（如 /admin/orders、/game/123、/ 本身）forward 到 /index.html
     *
     * 该 Bean 同时也是 Filter（OncePerRequestFilter），Spring Boot 会自动注册。
     */
    @org.springframework.context.annotation.Bean
    public org.springframework.web.filter.OncePerRequestFilter spaFallbackFilter() {
        return new OncePerRequestFilter() {
            // 明确归属于后端 API、文件上传、静态资源的前缀——全部跳过
            private static final String[] API_PREFIXES = {
                    "/api/", "/uploads/", "/assets/", "/picture/",
                    "/favicon.ico", "/icons.svg"
            };

            // 合法静态资源扩展名——命中则不 fallback
            private static final String[] STATIC_EXTS = {
                    ".js", ".mjs", ".css", ".png", ".jpg", ".jpeg",
                    ".gif", ".svg", ".ico", ".webp", ".woff", ".woff2",
                    ".ttf", ".eot", ".otf", ".json", ".map", ".txt", ".html"
            };

            @Override
            protected boolean shouldNotFilter(HttpServletRequest request) {
                if (!spaFallback) {
                    return true;
                }
                String uri = request.getRequestURI();
                // 先检查前缀
                for (String p : API_PREFIXES) {
                    if (uri.startsWith(p)) {
                        return true;
                    }
                }
                // 再检查扩展名
                int dot = uri.lastIndexOf('.');
                if (dot >= 0) {
                    String ext = uri.substring(dot).toLowerCase();
                    for (String e : STATIC_EXTS) {
                        if (ext.equals(e)) {
                            return true;
                        }
                    }
                }
                // 根路径或无扩展名路径——需要 fallback（但先确认 index.html 存在）
                return false;
            }

            @Override
            protected void doFilterInternal(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain filterChain)
                    throws ServletException, IOException {
                // 确认 classpath 里有 index.html（否则 filterChain 继续走会自然 404）
                ClassPathResource idx = new ClassPathResource("static/index.html");
                if (!idx.exists()) {
                    filterChain.doFilter(request, response);
                    return;
                }
                // forward 到 Spring 的 default servlet / 静态资源处理器
                request.getRequestDispatcher("/index.html").forward(request, response);
            }
        };
    }
}
