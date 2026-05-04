package com.yachiyo.QQBotService.dto.file;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UploadFileRequest {
    private String fileName; // 文件名
    private String anyUrl;   // httpUrl或localUrl
}
