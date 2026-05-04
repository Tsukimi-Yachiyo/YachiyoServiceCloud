package com.yachiyo.QQBotService.service.impl;

import com.yachiyo.QQBotService.client.FileClient;
import com.yachiyo.QQBotService.dto.file.UploadFileRequest;
import com.yachiyo.QQBotService.result.Result;
import com.yachiyo.QQBotService.service.FileService;
import com.yachiyo.QQBotService.support.ByteArrayMultipartFile;
import com.yachiyo.QQBotService.utils.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class FileServiceImpl implements FileService {
    private static final long URL_EXPIRE_SECONDS = 3600L;

    @Autowired
    private FileClient fileClient;

    @Autowired
    private FileUtils fileUtils;

    @Override
    public Result<String> uploadFile(String fileName, String anyUrl) {
        try {
            MultipartFile multipartFile = fileUtils.computeMultipartFile(fileName, anyUrl);
            return this.uploadFile(multipartFile);
        } catch (Exception e) {
            return Result.error("500", "上传文件失败", e.getMessage());
        }
    }

    @Override
    public Result<String> uploadFile(MultipartFile multipartFile) {
        try {
            if (multipartFile == null || multipartFile.isEmpty()) {
                return Result.error("500", "文件状态异常", "文件内容为空");
            }

            String minioFileName = computeFileName(multipartFile.getOriginalFilename());

            boolean uploaded = fileClient.upload(minioFileName, multipartFile);
            if (uploaded) {
                return Result.success(minioFileName);
            } else {
                return Result.error("500", "上传文件失败", minioFileName);
            }
        } catch (Exception e) {
            return Result.error("500", "上传文件失败", e.getMessage());
        }
    }

    @Override
    public Result<String> uploadFile(String fileName, byte[] content, String contentType) {
        try {
            if (content == null || content.length == 0) {
                return Result.error("500", "文件状态异常", "文件内容为空");
            }

            MultipartFile multipartFile = new ByteArrayMultipartFile(
                    fileName, contentType, content
            );

            return this.uploadFile(multipartFile);
        } catch (Exception e) {
            return Result.error("500", "上传文件失败", e.getMessage());
        }
    }

    @Override
    public Result<String> uploadFile(UploadFileRequest request) {
        return this.uploadFile(request.getFileName(), request.getAnyUrl());
    }

    @Override
    public Result<List<String>> uploadFiles(List<UploadFileRequest> requestList) {
        if (requestList == null || requestList.isEmpty()) {
            return Result.error("400", "上传列表不能为空", null);
        }

        try {
            List<String> uploadedFileNames = new ArrayList<>();

            for (var request : requestList) {
                uploadedFileNames.add(this.uploadFile(request).getData());
            }

            return Result.success(uploadedFileNames);
        } catch (Exception e) {
            return Result.error("500", "批量上传文件失败", e.getMessage());
        }
    }

    @Override
    public Result<String> getUrl(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return Result.error("400", "fileName不能为空", null);
        }

        try {
            String url = fileClient.getUrl(fileName, URL_EXPIRE_SECONDS, "robot");
            if (url == null || url.isBlank()) {
                return Result.error("404", "文件不存在或URL生成失败", fileName);
            }
            return Result.success(url);
        } catch (Exception e) {
            return Result.error("500", "生成URL失败", e.getMessage());
        }
    }

    @Override
    public Result<List<String>> getUrlList(List<String> fileNameList) {
        if (fileNameList == null || fileNameList.isEmpty()) {
            return Result.error("400", "fileNameList不能为空", null);
        }

        List<String> urlList = new ArrayList<>();
        try {
            for (int i = 0; i < fileNameList.size(); i++) {
                String fileName = fileNameList.get(i);
                if (fileName == null || fileName.isBlank()) {
                    log.error("第{}个fileName无效", i);
                    continue;
                }

                String url = fileClient.getUrl(fileName, URL_EXPIRE_SECONDS, "robot");
                if (url == null || url.isBlank()) {
                    log.error("第{}个文件URL生成失败，文件名：{}", i, fileName);
                    continue;
                }
                urlList.add(url);
            }
            return Result.success(urlList);
        } catch (Exception e) {
            return Result.error("500", "批量获取URL失败", e.getMessage());
        }
    }

    /**
     * 根据原始文件名生成一个新的文件名，使用 UUID 作为基础，并保留原始文件的扩展名
     * @param originalFileName 原始文件名
     * @return 新的文件名，格式为 UUID + 原始扩展名
     */
    private String computeFileName(String originalFileName) {
        return UUID.randomUUID() + findExtension(originalFileName);
    }

    /**
     * 获取文件扩展名，不是网络传输内容类型
     * @param fileName 文件名
     * @return 文件扩展名
     */
    private String findExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }

        int lastDotIndex = fileName.lastIndexOf('.');

        // 没有点或点在结尾
        if (lastDotIndex == -1 || lastDotIndex == fileName.length() - 1) {
            return "";
        }

        // 点是第一个字符（如 ".hidden"），返回整个文件名作为扩展名
        if (lastDotIndex == 0) {
            return fileName;
        }

        // 扩展名
        return fileName.substring(lastDotIndex);
    }
}
