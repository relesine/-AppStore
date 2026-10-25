package com.appstore.service.impl;

import com.appstore.common.ResultCode;
import com.appstore.exception.BusinessException;
import com.appstore.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * 文件上传服务实现
 * 安全说明：
 * 1. 扩展名白名单校验（防上传恶意可执行文件）
 * 2. MIME类型二次校验（防伪装扩展名）
 * 3. 上传目录白名单校验（防路径穿越攻击）
 */
@Slf4j
@Service
public class FileServiceImpl implements FileService {

    @Value("${file.upload-path}")
    private String uploadPath;

    @Value("${file.access-path}")
    private String accessPath;

    /** 允许的图片扩展名 */
    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList("jpg", "jpeg", "png", "gif", "webp");

    /** 允许的应用安装包扩展名 */
    private static final List<String> ALLOWED_APK_TYPES = Arrays.asList("apk", "exe", "zip", "rar");

    /** 允许的图片MIME类型 */
    private static final Set<String> ALLOWED_IMAGE_MIMES = new HashSet<>(Arrays.asList(
            "image/jpeg", "image/png", "image/gif", "image/webp"));

    /** 允许的安装包MIME类型 */
    private static final Set<String> ALLOWED_APK_MIMES = new HashSet<>(Arrays.asList(
            "application/vnd.android.package-archive",
            "application/octet-stream",
            "application/x-msdownload",
            "application/zip",
            "application/x-rar-compressed",
            "application/x-zip-compressed"));

    /** 允许的上传子目录（白名单，防路径穿越） */
    private static final Set<String> ALLOWED_DIRECTORIES = new HashSet<>(Arrays.asList("icons", "apps", "avatars"));

    @Override
    public String uploadFile(MultipartFile file, String directory) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ResultCode.FILE_UPLOAD_ERROR);
        }
        // 安全加固：目录白名单校验，防止路径穿越（如 /file/upload/../config）
        if (directory == null || !ALLOWED_DIRECTORIES.contains(directory)) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "不允许上传到该目录");
        }
        String originalFilename = file.getOriginalFilename();
        String ext = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase()
                : "";
        // 扩展名黑名单兜底：禁止可执行脚本
        if (isForbiddenExtension(ext)) {
            throw new BusinessException(ResultCode.FILE_TYPE_ERROR);
        }
        String newFilename = UUID.randomUUID().toString().replace("-", "") + "." + ext;
        String dirPath = uploadPath + directory + File.separator;
        File dir = new File(dirPath);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new BusinessException(ResultCode.FILE_UPLOAD_ERROR.getCode(), "创建上传目录失败");
        }
        try {
            file.transferTo(new File(dirPath + newFilename));
        } catch (IOException e) {
            log.error("文件保存失败: {}", e.getMessage());
            throw new BusinessException(ResultCode.FILE_UPLOAD_ERROR);
        }
        log.info("文件上传成功: {}{}/{}", accessPath, directory, newFilename);
        return accessPath + directory + "/" + newFilename;
    }

    @Override
    public String uploadIcon(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ResultCode.FILE_UPLOAD_ERROR);
        }
        String originalFilename = file.getOriginalFilename();
        String ext = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase()
                : "";
        if (!ALLOWED_IMAGE_TYPES.contains(ext)) {
            throw new BusinessException(ResultCode.FILE_TYPE_ERROR);
        }
        // MIME类型二次校验，防伪装扩展名
        String mime = file.getContentType();
        if (mime != null && !mime.isEmpty() && !ALLOWED_IMAGE_MIMES.contains(mime)) {
            throw new BusinessException(ResultCode.FILE_TYPE_ERROR.getCode(), "图片格式不合法");
        }
        // 限制图标大小5MB
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new BusinessException(ResultCode.FILE_SIZE_ERROR);
        }
        return uploadFile(file, "icons");
    }

    @Override
    public String uploadApk(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ResultCode.FILE_UPLOAD_ERROR);
        }
        String originalFilename = file.getOriginalFilename();
        String ext = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase()
                : "";
        if (!ALLOWED_APK_TYPES.contains(ext)) {
            throw new BusinessException(ResultCode.FILE_TYPE_ERROR);
        }
        String mime = file.getContentType();
        if (mime != null && !mime.isEmpty() && !ALLOWED_APK_MIMES.contains(mime)) {
            throw new BusinessException(ResultCode.FILE_TYPE_ERROR.getCode(), "安装包格式不合法");
        }
        return uploadFile(file, "apps");
    }

    /**
     * 危险扩展名黑名单（防上传脚本/可执行文件伪装）
     */
    private boolean isForbiddenExtension(String ext) {
        if (ext == null || ext.isEmpty()) {
            return true;
        }
        String lower = ext.toLowerCase();
        return lower.equals("jsp") || lower.equals("jspx") || lower.equals("php")
                || lower.equals("asp") || lower.equals("aspx") || lower.equals("sh")
                || lower.equals("bat") || lower.equals("cmd") || lower.equals("html")
                || lower.equals("htm") || lower.equals("js") || lower.equals("vbs")
                || lower.equals("jar") || lower.equals("war");
    }
}
