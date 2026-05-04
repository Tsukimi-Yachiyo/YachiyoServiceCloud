package com.yachiyo.QQBotService.service;

import com.yachiyo.QQBotService.dto.file.UploadFileRequest;
import com.yachiyo.QQBotService.result.Result;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FileService {
    Result<String> uploadFile(String fileName, String anyUrl);

    /**
     * 上传文件到 MinIO，并返回存储的文件名
     * @param multipartFile MultipartFile 实现
     * @return 期望为 MinIO 存储的文件名
     */
    Result<String> uploadFile(MultipartFile multipartFile);

    /**
     * 上传文件到 MinIO，并返回存储的文件名
     * @param fileName 原始文件名
     * @param content 文件内容的字节数组
     * @param contentType MIME 类型
     * @return 期望为 MinIO 存储的文件名
     */
    Result<String> uploadFile(String fileName, byte[] content, String contentType);

    /**
     * 上传文件到 MinIO，并返回存储的文件名
     * @param uploadFileRequest 上传请求，包含原始文件名和临时URL
     * @return 期望为 MinIO 存储的文件名
     */
    Result<String> uploadFile(UploadFileRequest uploadFileRequest);

    /**
     * 批量上传文件到 MinIO，并返回文件名列表
     * @param uploadFileRequestList 上传请求列表，包含原始文件名和临时URL
     * @return 期望为 MinIO 存储的文件名列表
     */
    Result<List<String>> uploadFiles(List<UploadFileRequest> uploadFileRequestList);

    /**
     * 根据文件名获取对应的 MinIO 下载 URL
     * @param fileName MinIO 存储的文件名
     * @return 对应的 MinIO 下载 URL
     */
    Result<String> getUrl(String fileName);

    /**
     * 根据文件名列表获取对应的 MinIO 下载 URL 列表
     * @param fileNameList 文件名列表
     * @return 对应的 MinIO 下载 URL 列表，顺序与输入文件名列表一致
     */
    Result<List<String>> getUrlList(List<String> fileNameList);
}
