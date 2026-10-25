package com.appstore.controller;

import com.appstore.common.PageResult;
import com.appstore.common.Result;
import com.appstore.service.DownloadService;
import com.appstore.vo.AppInfoVO;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/download")
public class DownloadController {

    @Resource
    private DownloadService downloadService;

    @PostMapping("/{appId}")
    public Result<String> downloadApp(@PathVariable Long appId, HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        return Result.success(downloadService.downloadApp(appId, ip));
    }

    @GetMapping("/my")
    public Result<PageResult<AppInfoVO>> getMyDownloads(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(downloadService.getMyDownloads(pageNum, pageSize));
    }
}
