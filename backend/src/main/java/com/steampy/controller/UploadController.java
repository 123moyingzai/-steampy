package com.steampy.controller;

import com.steampy.dto.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
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
}
