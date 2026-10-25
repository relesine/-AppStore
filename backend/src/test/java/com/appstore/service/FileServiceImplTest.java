package com.appstore.service;

import com.appstore.common.ResultCode;
import com.appstore.exception.BusinessException;
import com.appstore.service.impl.FileServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 文件上传服务单元测试
 * 覆盖：目录白名单（防路径穿越）、扩展名黑名单（防上传可执行脚本）
 */
class FileServiceImplTest {

    private final FileServiceImpl fileService = new FileServiceImpl();

    // ---------- 目录白名单（防路径穿越） ----------

    @Test
    @DisplayName("非法目录（路径穿越）被拒绝")
    void uploadFile_shouldRejectTraversalDirectory() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.png", "image/png", new byte[]{1, 2, 3});
        BusinessException ex = assertThrows(BusinessException.class,
                () -> fileService.uploadFile(file, "../config"));
        assertEquals(ResultCode.PARAM_ERROR.getCode(), ex.getCode());
    }

    @Test
    @DisplayName("空目录名被拒绝")
    void uploadFile_shouldRejectNullDirectory() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.png", "image/png", new byte[]{1, 2, 3});
        BusinessException ex = assertThrows(BusinessException.class,
                () -> fileService.uploadFile(file, null));
        assertEquals(ResultCode.PARAM_ERROR.getCode(), ex.getCode());
    }

    // ---------- 扩展名黑名单（防上传可执行脚本） ----------

    @Test
    @DisplayName("JSP脚本文件被拒绝上传")
    void uploadFile_shouldRejectJsp() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "shell.jsp", "application/octet-stream", "code".getBytes());
        BusinessException ex = assertThrows(BusinessException.class,
                () -> fileService.uploadFile(file, "apps"));
        assertEquals(ResultCode.FILE_TYPE_ERROR.getCode(), ex.getCode());
    }

    @Test
    @DisplayName("PHP脚本文件被拒绝上传")
    void uploadFile_shouldRejectPhp() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "backdoor.php", "application/octet-stream", "code".getBytes());
        BusinessException ex = assertThrows(BusinessException.class,
                () -> fileService.uploadFile(file, "apps"));
        assertEquals(ResultCode.FILE_TYPE_ERROR.getCode(), ex.getCode());
    }

    @Test
    @DisplayName("HTML文件被拒绝上传（防存储型XSS）")
    void uploadFile_shouldRejectHtml() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "page.html", "text/html", "<script>".getBytes());
        BusinessException ex = assertThrows(BusinessException.class,
                () -> fileService.uploadFile(file, "icons"));
        assertEquals(ResultCode.FILE_TYPE_ERROR.getCode(), ex.getCode());
    }

    // ---------- 图标上传 ----------

    @Test
    @DisplayName("非图片扩展名的图标被拒绝")
    void uploadIcon_shouldRejectNonImage() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "icon.txt", "text/plain", "data".getBytes());
        BusinessException ex = assertThrows(BusinessException.class,
                () -> fileService.uploadIcon(file));
        assertEquals(ResultCode.FILE_TYPE_ERROR.getCode(), ex.getCode());
    }
}
