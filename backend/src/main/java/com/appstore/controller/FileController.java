package com.appstore.controller;

import com.appstore.common.Result;
import com.appstore.service.FileService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

@RestController
@RequestMapping("/file")
public class FileController {

    @Resource
    private FileService fileService;

    @PostMapping("/upload/icon")
    public Result<String> uploadIcon(@RequestParam("file") MultipartFile file) {
        return Result.success(fileService.uploadIcon(file));
    }

    @PostMapping("/upload/apk")
    public Result<String> uploadApk(@RequestParam("file") MultipartFile file) {
        return Result.success(fileService.uploadApk(file));
    }

    @PostMapping("/upload/{directory}")
    public Result<String> uploadFile(@RequestParam("file") MultipartFile file,
                                      @PathVariable String directory) {
        return Result.success(fileService.uploadFile(file, directory));
    }
}
