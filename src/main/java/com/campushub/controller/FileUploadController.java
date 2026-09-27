package com.campushub.controller;

import com.campushub.common.Result;
import com.campushub.common.exception.BusinessException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/uploads")
public class FileUploadController {
    private static final Set<String> ALLOWED_TYPES = Set.of("image/jpeg", "image/png", "image/webp", "image/gif");

    @PostMapping("/images")
    public Result<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) throw new BusinessException(400, "请选择图片");
        if (file.getSize() > 5L * 1024 * 1024) throw new BusinessException(400, "图片不能超过5MB");
        if (!ALLOWED_TYPES.contains(file.getContentType())) throw new BusinessException(400, "仅支持 JPG、PNG、WEBP 或 GIF 图片");
        if (!hasValidSignature(file, file.getContentType())) throw new BusinessException(400, "文件内容与图片格式不符");

        String extension = switch (file.getContentType()) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            case "image/gif" -> ".gif";
            default -> throw new BusinessException(400, "不支持的图片类型");
        };
        String filename = UUID.randomUUID() + extension;
        Path directory = Path.of("uploads").toAbsolutePath().normalize();
        try {
            Files.createDirectories(directory);
            file.transferTo(directory.resolve(filename));
        } catch (IOException exception) {
            throw new BusinessException(500, "图片保存失败");
        }
        return Result.success(Map.of("url", "/uploads/" + filename));
    }

    private boolean hasValidSignature(MultipartFile file, String contentType) {
        try (InputStream input = file.getInputStream()) {
            byte[] header = input.readNBytes(12);
            return switch (contentType) {
                case "image/jpeg" -> header.length >= 3
                        && (header[0] & 0xFF) == 0xFF && (header[1] & 0xFF) == 0xD8 && (header[2] & 0xFF) == 0xFF;
                case "image/png" -> header.length >= 8
                        && Arrays.equals(Arrays.copyOf(header, 8), new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A});
                case "image/gif" -> header.length >= 6
                        && (new String(header, 0, 6, java.nio.charset.StandardCharsets.US_ASCII).equals("GIF87a")
                        || new String(header, 0, 6, java.nio.charset.StandardCharsets.US_ASCII).equals("GIF89a"));
                case "image/webp" -> header.length >= 12
                        && new String(header, 0, 4, java.nio.charset.StandardCharsets.US_ASCII).equals("RIFF")
                        && new String(header, 8, 4, java.nio.charset.StandardCharsets.US_ASCII).equals("WEBP");
                default -> false;
            };
        } catch (IOException exception) {
            return false;
        }
    }
}
