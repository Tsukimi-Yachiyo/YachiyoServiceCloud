package com.yachiyo.FileService.controller;

import com.yachiyo.FileService.utils.FileUrlUtil;
import io.minio.GetObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.MinioClient;
import io.minio.errors.ErrorResponseException;
import lombok.RequiredArgsConstructor;
import org.apache.tika.metadata.HttpHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequiredArgsConstructor
@RequestMapping("/file")
public class FileController {

    private final FileUrlUtil fileUrlUtil;

    private final MinioClient minioClient;

    // 从配置文件读取MinIO桶名称
    @Value("${minio.bucketName}")
    private String bucketName;

    @GetMapping("/generate")
    public ResponseEntity<Resource> generateFileUrl(@RequestParam String fileName,
                                                    @RequestParam long expire,
                                                    @RequestParam String sign) {
        // 1. 文件名解码（保留原有逻辑）
        fileName = URLDecoder.decode(fileName, StandardCharsets.UTF_8);

        // 2. 签名校验失败 → 403（保留原有逻辑）
        if (!fileUrlUtil.verify(fileName, expire, sign)){
            return ResponseEntity.status(403).body(null);
        }

        try {
            // 3. 检查MinIO中文件是否存在（替换本地文件判断）
            minioClient.statObject(StatObjectArgs.builder()
                    .bucket(bucketName)
                    .object(fileName)
                    .build());

            // 4. 从MinIO获取文件流（替换本地文件流）
            InputStream inputStream = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(fileName)
                    .build());
            InputStreamResource resource = new InputStreamResource(inputStream);

            // 5. 原样返回响应头、Content-Type（和你原来完全一致）
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .header("Access-Control-Expose-Headers", "Content-Disposition")
                    .body(resource);

        } catch (ErrorResponseException e) {
            // MinIO文件不存在 → 404
            if (e.response().code() == 404) {
                return ResponseEntity.status(404).body(null);
            }
            // 其他MinIO异常 → 500
            return ResponseEntity.status(500).body(null);
        } catch (Exception e) {
            // 系统异常
            return ResponseEntity.status(500).body(null);
        }
    }
}