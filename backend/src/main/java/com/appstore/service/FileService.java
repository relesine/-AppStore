package com.appstore.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileService {
    String uploadFile(MultipartFile file, String directory);
    String uploadIcon(MultipartFile file);
    String uploadApk(MultipartFile file);
}
