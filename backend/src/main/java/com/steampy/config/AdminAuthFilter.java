package com.steampy.config;

import com.steampy.entity.User;
import com.steampy.mapper.UserMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 管理后台接口鉴权过滤器
 *
 * 规则：
 *  - 拦截所有 /api/admin/** 请求（OPTIONS 预检放行）
 *  - 请求头必须携带 X-Admin-Id（当前登录管理员的 userId）
 *  - 该 id 对应用户必须存在且 user_type = 管理员，否则一律 403
 *  - 前端由 AdminLayout.vue 挂载时统一注入 axios 全局请求头
 *
 * 背景：此前 26 个 /api/admin/** 接口（封禁/删用户/改订单/提现审核等）无任何
 *      鉴权，匿名 curl 即可调用，属高危漏洞；本过滤器统一收敛。
 */
@Component
@Order(0)
public class AdminAuthFilter extends OncePerRequestFilter {

    @Autowired
    private UserMapper userMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String uri = request.getRequestURI();
        if (!uri.startsWith("/api/admin/") || "OPTIONS".equalsIgnoreCase(request.getMethod())) {
            chain.doFilter(request, response);
            return;
        }

        String adminId = request.getHeader("X-Admin-Id");
        boolean isAdmin = false;
        if (adminId != null && !adminId.isBlank()) {
            User u = userMapper.selectById(adminId);
            isAdmin = u != null && "管理员".equals(u.getUserType());
        }
        if (!isAdmin) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":403,\"message\":\"需要管理员权限\",\"data\":null}");
            return;
        }

        chain.doFilter(request, response);
    }
}
