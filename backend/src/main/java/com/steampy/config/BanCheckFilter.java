package com.steampy.config;

import com.steampy.entity.User;
import com.steampy.mapper.UserMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 封禁用户拦截过滤器
 *
 * 规则（正常的封禁语义）：
 *  - 已封禁用户（user_type = 已封禁）可以登录、修改资料、修改密码（/api/auth/** 放行）
 *  - 可以浏览公开内容（不带身份的请求，如游戏列表、公告）
 *  - 但任何携带其身份的业务操作（下单、上架、钱包、评价、收藏、Steam 等）一律 403 拒绝
 *
 * 身份识别：query 参数（userId/sellerId/reporterId）→ 路径段（/user/{id}、/seller/{id}）
 *          → JSON body（user_id/userId/buyer_id/reporterId）
 */
@Component
@Order(1)
public class BanCheckFilter extends OncePerRequestFilter {

    /** 路径中的用户身份段：/xxx/user/{id} 或 /xxx/seller/{id} */
    private static final Pattern PATH_ID = Pattern.compile("(?:user|seller)/([A-Za-z0-9\\-]{6,40})");

    /** JSON body 中的请求者身份字段（均为「当前操作者本人」语义，不含 seller_id——那是交易对手） */
    private static final Pattern BODY_ID =
            Pattern.compile("\"(?:user_id|userId|buyer_id|reporterId)\"\\s*:\\s*\"([A-Za-z0-9\\-]{6,40})\"");

    @Autowired
    private UserMapper userMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String uri = request.getRequestURI();

        // 只拦业务 API；认证（登录/注册/资料/密码）与管理员接口放行
        if (!uri.startsWith("/api/") || uri.startsWith("/api/auth") || uri.startsWith("/api/admin")) {
            chain.doFilter(request, response);
            return;
        }

        // 有 JSON body 的请求先包一层可重复读，避免消费掉流导致 Controller 读不到
        String method = request.getMethod();
        String contentType = request.getContentType();
        boolean hasJsonBody = contentType != null && contentType.contains("application/json")
                && !"GET".equalsIgnoreCase(method) && !"HEAD".equalsIgnoreCase(method);

        HttpServletRequest req = hasJsonBody ? new CachedBodyRequestWrapper(request) : request;

        String userId = resolveUserId(req, hasJsonBody);
        if (userId != null && !userId.isBlank() && isBanned(userId)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":403,\"message\":\"您的账号已被封禁，暂时无法使用该功能，如有疑问请联系管理员\",\"data\":null}");
            return;
        }

        chain.doFilter(req, response);
    }

    /** 依次从 query 参数 → 路径段 → JSON body 中解析请求者身份 */
    private String resolveUserId(HttpServletRequest request, boolean hasJsonBody) {
        // 1) query 参数
        for (String p : new String[]{"userId", "sellerId", "reporterId"}) {
            String v = request.getParameter(p);
            if (v != null && !v.isBlank()) return v.trim();
        }
        // 2) 路径段 /user/{id} /seller/{id}
        Matcher m = PATH_ID.matcher(request.getRequestURI());
        if (m.find()) return m.group(1);
        // 3) JSON body
        if (hasJsonBody && request instanceof CachedBodyRequestWrapper wrapped) {
            String body = wrapped.getBody();
            if (body != null && !body.isBlank()) {
                Matcher bm = BODY_ID.matcher(body);
                if (bm.find()) return bm.group(1);
            }
        }
        return null;
    }

    private boolean isBanned(String userId) {
        try {
            User u = userMapper.selectById(userId);
            return u != null && "已封禁".equals(u.getUserType());
        } catch (Exception e) {
            // 查询异常不阻断请求，避免过滤器本身引发故障
            return false;
        }
    }

    /** 缓存 body 的请求包装器：过滤器读完后 Controller 仍可正常读取 */
    static class CachedBodyRequestWrapper extends HttpServletRequestWrapper {
        private final byte[] cached;

        CachedBodyRequestWrapper(HttpServletRequest request) throws IOException {
            super(request);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            try (InputStream is = request.getInputStream()) {
                is.transferTo(bos);
            }
            cached = bos.toByteArray();
        }

        String getBody() {
            return new String(cached, StandardCharsets.UTF_8);
        }

        @Override
        public ServletInputStream getInputStream() {
            ByteArrayInputStream bis = new ByteArrayInputStream(cached);
            return new ServletInputStream() {
                @Override
                public boolean isFinished() {
                    return bis.available() == 0;
                }

                @Override
                public boolean isReady() {
                    return true;
                }

                @Override
                public void setReadListener(ReadListener listener) {
                    // 不需要异步读
                }

                @Override
                public int read() {
                    return bis.read();
                }
            };
        }

        @Override
        public BufferedReader getReader() {
            return new BufferedReader(new InputStreamReader(getInputStream(), StandardCharsets.UTF_8));
        }
    }
}
