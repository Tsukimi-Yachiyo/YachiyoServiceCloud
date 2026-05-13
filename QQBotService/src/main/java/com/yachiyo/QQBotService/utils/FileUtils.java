package com.yachiyo.QQBotService.utils;

import com.yachiyo.QQBotService.support.ByteArrayMultipartFile;
import kong.unirest.core.HttpResponse;
import kong.unirest.core.Unirest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Component
public class FileUtils {
    public MultipartFile computeMultipartFile(String fileName, String anyUrl) {
        if (anyUrl.startsWith("http://") || anyUrl.startsWith("https://")) {
            return downloadFile(fileName, anyUrl);
        } else {
            return readFileToByteArray(fileName, anyUrl);
        }
    }

    public MultipartFile downloadFile(String fileName, String httpUrl) {
        try {
            HttpResponse<byte[]> response = Unirest.get(httpUrl).asBytes();
            int status = response.getStatus();
            if (status < 200 || status >= 300) {
                log.error("下载文件失败: HTTP {}", status);
                return null;
            }

            byte[] body = response.getBody();
            if (body == null || body.length == 0) {
                log.error("下载文件失败: 文件内容为空");
                return null;
            }

            String contentType = response.getHeaders().getFirst("Content-Type");
            if (contentType == null || contentType.isBlank()) {
                contentType = "application/octet-stream";
            }

            return new ByteArrayMultipartFile(
                    fileName, contentType, body
            );
        } catch (Exception e) {
            log.error("下载文件失败: {}", e.getMessage());
            return null;
        }
    }

    public MultipartFile readFileToByteArray(String fileName, String localUrl) {
        try {
            Path path = Paths.get(localUrl);
            byte[] data = Files.readAllBytes(path);
            String contentType = Files.probeContentType(path);
            if (contentType == null || contentType.isBlank()) {
                contentType = "application/octet-stream";
            }

            return new ByteArrayMultipartFile(
                    fileName, contentType, data
            );

        } catch (java.io.IOException e) {
            log.error("读取文件失败: {}", e.getMessage());
            return null;
        }
    }
}
