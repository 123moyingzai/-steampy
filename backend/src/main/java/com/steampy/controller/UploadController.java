package com.steampy.controller;

import com.steampy.dto.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

@RestController
@RequestMapping("/api/upload")
public class UploadController {

    /**
     * 下载公网封面图专用 HttpClient。
     * JDK 自带信任库缺少部分 CDN 的完整证书链（实测 cdn.akamai.steamstatic.com
     * 报 PKIX path building failed），这里只用于拉取公开图片，因此放开证书校验，
     * 不用于任何携带凭据的请求。
     */
    private final HttpClient httpClient = buildImageClient();

    private static HttpClient buildImageClient() {
        try {
            SSLContext ctx = SSLContext.getInstance("TLS");
            ctx.init(null, new TrustManager[]{new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) {
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            }}, new SecureRandom());
            return HttpClient.newBuilder()
                    .sslContext(ctx)
                    .followRedirects(HttpClient.Redirect.NORMAL)
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();
        } catch (Exception e) {
            return HttpClient.newBuilder()
                    .followRedirects(HttpClient.Redirect.NORMAL)
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();
        }
    }

    // 上传目录（相对于项目根）
    @Value("${app.upload-dir:uploads}")
    private String uploadDir;

    @PostMapping("/avatar")
    public Result<Map<String, String>> uploadAvatar(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return Result.error("请选择要上传的图片");
        }
        String original = file.getOriginalFilename();
        if (original == null) return Result.error("文件名无效");

        String lower = original.toLowerCase();
        if (!lower.endsWith(".jpg") && !lower.endsWith(".jpeg") && !lower.endsWith(".png")
                && !lower.endsWith(".gif") && !lower.endsWith(".webp")) {
            return Result.error("只支持 jpg/png/gif/webp 格式");
        }
        if (file.getSize() > 5 * 1024 * 1024) {
            return Result.error("图片不能超过 5MB");
        }

        File dir = new File(uploadDir, "avatars");
        if (!dir.exists()) dir.mkdirs();
        File absDir = dir.getAbsoluteFile();

        String ext = lower.substring(lower.lastIndexOf('.'));
        String filename = UUID.randomUUID().toString().replace("-", "") + ext;
        File dest = new File(absDir, filename);
        try {
            file.transferTo(dest.getAbsoluteFile());
        } catch (IOException e) {
            return Result.error("保存图片失败：" + e.getMessage());
        }

        Map<String, String> resp = new HashMap<>();
        resp.put("url", "/uploads/avatars/" + filename);
        resp.put("filename", filename);
        return Result.success(resp);
    }

    /**
     * 评价配图上传（存到 uploads/reviews/，返回路径供 DB 存储）
     */
    @PostMapping("/review-image")
    public Result<Map<String, String>> uploadReviewImage(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return Result.error("请选择要上传的图片");
        }
        String original = file.getOriginalFilename();
        if (original == null) return Result.error("文件名无效");

        String lower = original.toLowerCase();
        if (!lower.endsWith(".jpg") && !lower.endsWith(".jpeg") && !lower.endsWith(".png")
                && !lower.endsWith(".gif") && !lower.endsWith(".webp")) {
            return Result.error("只支持 jpg/png/gif/webp 格式");
        }
        if (file.getSize() > 5 * 1024 * 1024) {
            return Result.error("图片不能超过 5MB");
        }

        File dir = new File(uploadDir, "reviews");
        if (!dir.exists()) dir.mkdirs();
        File absDir = dir.getAbsoluteFile();

        String ext = lower.substring(lower.lastIndexOf('.'));
        String filename = UUID.randomUUID().toString().replace("-", "") + ext;
        File dest = new File(absDir, filename);
        try {
            file.transferTo(dest.getAbsoluteFile());
        } catch (IOException e) {
            return Result.error("保存图片失败：" + e.getMessage());
        }

        Map<String, String> resp = new HashMap<>();
        resp.put("url", "/uploads/reviews/" + filename);
        resp.put("filename", filename);
        return Result.success(resp);
    }

    /**
     * 远程图片下载到服务器本地（供管理端 AI 自动填写后下载封面用）
     * body: { url: "https://cdn.akamai.steamstatic.com/..." }
     * 返回 { url: "/uploads/games/xxx.jpg" }
     */
    @PostMapping("/remote-image")
    public Result<Map<String, String>> downloadRemoteImage(@RequestBody Map<String, String> body) {
        String url = body.getOrDefault("url", "").trim();
        if (url.isBlank()) return Result.error("图片地址不能为空");
        if (!url.startsWith("http://") && !url.startsWith("https://"))
            return Result.error("只支持 http/https 地址");

        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(30))
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) SteamPY")
                    .GET()
                    .build();
            HttpResponse<Path> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofFile(
                    Files.createTempFile("steampy-img-", ".tmp")));

            if (resp.statusCode() != 200) {
                Files.deleteIfExists(resp.body());
                return Result.error("下载失败: HTTP " + resp.statusCode());
            }

            // 从 Content-Type 或 URL 推断扩展名
            String contentType = resp.headers().firstValue("Content-Type").orElse("image/jpeg");
            String ext = ".jpg";
            if (contentType.contains("png")) ext = ".png";
            else if (contentType.contains("gif")) ext = ".gif";
            else if (contentType.contains("webp")) ext = ".webp";

            // 也尝试从 URL 里拿
            String pathPart = URI.create(url).getPath();
            if (pathPart != null) {
                int dot = pathPart.lastIndexOf('.');
                if (dot >= 0 && dot < pathPart.length() - 1) {
                    String urlExt = pathPart.substring(dot).toLowerCase();
                    if (urlExt.length() <= 5 && (urlExt.endsWith(".jpg") || urlExt.endsWith(".jpeg")
                            || urlExt.endsWith(".png") || urlExt.endsWith(".gif") || urlExt.endsWith(".webp"))) {
                        ext = urlExt.equals(".jpeg") ? ".jpg" : urlExt;
                    }
                }
            }

            // 保存到 uploads/games/
            File dir = new File(uploadDir, "games");
            if (!dir.exists()) dir.mkdirs();
            File absDir = dir.getAbsoluteFile();
            String filename = UUID.randomUUID().toString().replace("-", "") + ext;
            File dest = new File(absDir, filename);
            Files.move(resp.body(), dest.toPath());

            // 超过 5MB 告警
            if (dest.length() > 5 * 1024 * 1024) {
                dest.delete();
                return Result.error("图片超过 5MB，拒绝保存");
            }

            Map<String, String> r = new HashMap<>();
            r.put("url", "/uploads/games/" + filename);
            return Result.success(r);

        } catch (Exception e) {
            return Result.error("下载失败: " + e.getMessage());
        }
    }
}
