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
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * UploadController
 * 负责文件上传业务的 HTTP 接口层（当前仅支持图片上传）
 * 提供的核心接口：
 *   - POST /api/upload/avatar        — 上传用户头像（存到 uploads/avatars/）
 *   - POST /api/upload/review-image  — 上传评价配图（存到 uploads/reviews/）
 *
 * 两个接口共享相同的校验规则：
 *   - 文件不能为空
 *   - 仅允许 jpg / jpeg / png / gif / webp 格式
 *   - 单文件大小不超过 5MB
 *
 * @author steampy
 * @date 2026-10-14
 */
@RestController
@RequestMapping("/api/upload")
public class UploadController {

    // 上传根目录（相对路径，从项目运行目录下创建）
    @Value("${app.upload-dir:uploads}")
    private String uploadDir;

    /** 下载网络图片用的 HTTP 客户端（复用连接、跟随 302 重定向） */
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    /**
     * 上传用户头像图片
     * <p>
     * 校验文件格式和大小后，存入 uploads/avatars/ 子目录，
     * 返回可直接在前端使用的相对 URL 路径（/uploads/avatars/xxx.png）。
     * </p>
     *
     * @param file MultipartFile 上传文件（前端以 multipart/form-data 提交，参数名必须为 "file"）
     * @return {@link Result} 包含 url（可访问的相对路径）和 filename（存储的文件名）
     */
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
     * 上传游戏评测配图
     * <p>
     * 校验规则与头像上传一致，文件存入 uploads/reviews/ 子目录，
     * 返回相对 URL 路径供 Review.reviews 表存储。
     * </p>
     *
     * @param file MultipartFile 上传文件（参数名必须为 "file"）
     * @return {@link Result} 包含 url（/uploads/reviews/xxx.png）和 filename
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
     * 把网络图片下载到本地并返回可访问地址（新增游戏封面用）。
     * <p>
     * 后端管理端填写的封面多是 Steam CDN 地址，直接引用存在被墙/失效后
     * 页面显示空白的风险，这里下载一份到 uploads/games/ 长期可访问。
     * </p>
     *
     * @param body { url: 图片地址 }
     * @return {@link Result} 包含 url（/uploads/games/xxx.jpg）和 filename
     */
    @PostMapping("/remote-image")
    public Result<Map<String, String>> uploadRemoteImage(@RequestBody Map<String, String> body) {
        String url = body == null ? null : body.get("url");
        if (url == null || !url.startsWith("http")) {
            return Result.error("图片地址无效");
        }

        byte[] bytes;
        String contentType;
        try {
            HttpResponse<byte[]> resp = HTTP_CLIENT.send(
                    HttpRequest.newBuilder(URI.create(url))
                            .timeout(Duration.ofSeconds(15))
                            .header("User-Agent", "Mozilla/5.0")
                            .GET().build(),
                    HttpResponse.BodyHandlers.ofByteArray());
            if (resp.statusCode() != 200) {
                return Result.error("图片下载失败：HTTP " + resp.statusCode());
            }
            contentType = resp.headers().firstValue("content-type").orElse("");
            bytes = resp.body();
        } catch (Exception e) {
            return Result.error("图片下载失败：" + e.getMessage());
        }

        if (contentType != null && !contentType.isEmpty() && !contentType.toLowerCase().startsWith("image/")) {
            return Result.error("该地址返回的不是图片");
        }
        if (bytes == null || bytes.length == 0) {
            return Result.error("图片内容为空");
        }
        if (bytes.length > 5 * 1024 * 1024) {
            return Result.error("图片不能超过 5MB");
        }

        // 扩展名优先取 Content-Type，其次取 URL 后缀，最后按 jpg 兜底
        String ext = ".jpg";
        if (contentType != null) {
            String ct = contentType.toLowerCase();
            if (ct.contains("png")) ext = ".png";
            else if (ct.contains("webp")) ext = ".webp";
            else if (ct.contains("gif")) ext = ".gif";
            else if (ct.contains("jpeg") || ct.contains("jpg")) ext = ".jpg";
        }

        // Steam 封面地址形如 .../apps/{appid}/header.jpg，直接用 appid 命名便于排查
        String filename;
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("/apps/(\\d+)/").matcher(url);
        if (m.find()) {
            filename = m.group(1) + "_header" + ext;
        } else {
            filename = UUID.randomUUID().toString().replace("-", "") + ext;
        }

        File dir = new File(uploadDir, "games");
        if (!dir.exists()) dir.mkdirs();
        File dest = new File(dir.getAbsoluteFile(), filename);
        try {
            java.nio.file.Files.write(dest.toPath(), bytes);
        } catch (IOException e) {
            return Result.error("保存图片失败：" + e.getMessage());
        }

        Map<String, String> resp = new HashMap<>();
        resp.put("url", "/uploads/games/" + filename);
        resp.put("filename", filename);
        return Result.success(resp);
    }
}
