package com.steampy.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * SPA 路由转发：Vue Router (history 模式) 刷新子路径时
 * Spring Boot 默认返回 404，需要转发回 index.html 由前端路由处理。
 * 规则：非 /api/**、非 /assets/**、非 /picture/**、非 /favicon.ico 的 GET 请求 → index.html
 */
@Controller
public class SpaForwardController {

    @RequestMapping(value = {
            "/",
            "/game/**",
            "/login",
            "/register",
            "/settings",
            "/buyer",
            "/seller",
            "/wallet",
            "/transactions",
            "/mygames",
            "/help",
            "/admin/**",
            "/cdk-market",
            "/gift",
            "/cdk/**",
            "/pic/**"
    })
    public String forward() {
        return "forward:/index.html";
    }

    /** 兜底：所有没被 Controller / 静态资源匹配到的 GET 请求 → index.html */
    @RequestMapping(value = "/**")
    public String fallback(HttpServletRequest request) {
        String uri = request.getRequestURI();
        // 排除 API 和静态资源
        if (uri.startsWith("/api/") || uri.startsWith("/assets/")
                || uri.startsWith("/picture/") || uri.startsWith("/favicon")
                || uri.endsWith(".css") || uri.endsWith(".js")
                || uri.endsWith(".svg") || uri.endsWith(".png")
                || uri.endsWith(".jpg") || uri.endsWith(".ico")
                || uri.endsWith(".json") || uri.endsWith(".map")) {
            return "forward:/index.html"; // 静态资源找不到也回 index.html（前端自己处理）
        }
        return "forward:/index.html";
    }
}
