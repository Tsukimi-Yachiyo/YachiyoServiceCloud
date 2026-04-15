package com.yachiyo.FileService.controller.internal;

import com.yachiyo.FileService.utils.FileUrlUtil;
import com.yachiyo.FileService.utils.IOFileUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/file")
public class FileInternalController {

    private final FileUrlUtil fileUrlUtil;

    private final IOFileUtils ioFileUtils;

    @GetMapping("/getUrl")
    public String getUrl(@RequestParam("url") String url,
            @RequestParam("time") long time) {
        return fileUrlUtil.generateFileUrl(url, time);
    }

    @PutMapping("/upload")
    public boolean upload(
            @RequestParam("fileName") String fileName,
            @RequestParam(required = false) MultipartFile file) {
        return ioFileUtils.uploadFile(fileName, file);
    }

    @PutMapping("/save")
    public boolean save(
            @RequestParam("fileName") String fileName,
            @RequestParam(required = false) MultipartFile file) {
        return ioFileUtils.saveFile(fileName, file);
    }

    @DeleteMapping("/delete")
    public boolean delete(
            @RequestParam("fileName") String fileName) {
        ioFileUtils.deleteFile(fileName);
        return true;
    }

    @GetMapping("/read")
    public byte[] read(
            @RequestParam("fileName") String fileName) {
        return ioFileUtils.readFile(fileName);
    }

    @GetMapping("/checkExist")
    public boolean checkExist(
            @RequestParam("fileName") String fileName) {
        return ioFileUtils.fileExist(fileName);
    }

    @GetMapping("/getNames")
    public List<String> getNames(
            @RequestParam("dirName")  String dirName) throws IOException {
        return ioFileUtils.getFileNames(dirName);
    }
}
