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

@RestController
@RequestMapping("/api/upload")
public class UploadController {

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
}
