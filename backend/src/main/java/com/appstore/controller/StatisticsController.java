package com.appstore.controller;

import com.appstore.common.Result;
import com.appstore.service.*;
import com.appstore.util.UserContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * 统计控制器
 */
@RestController
@RequestMapping("/statistics")
public class StatisticsController {

    @Resource
    private UserService userService;

    @Resource
    private AppInfoService appInfoService;

    @Resource
    private DownloadService downloadService;

    @Resource
    private CommentService commentService;

    @Resource
    private NoticeService noticeService;

    /** 管理后台数据大盘 */
    @GetMapping("/dashboard")
    public Result<Map<String, Object>> getDashboard() {
        Map<String, Object> data = new HashMap<>();
        data.put("userCount", userService.getUserCount());
        data.put("todayNewUsers", userService.getTodayNewUserCount());
        data.put("appCount", appInfoService.getAppCount());
        data.put("pendingAuditCount", appInfoService.getPendingAuditCount());
        data.put("downloadCount", downloadService.getDownloadCount());
        data.put("todayDownloadCount", downloadService.getTodayDownloadCount());
        data.put("commentCount", commentService.getCommentCount());
        data.put("noticeCount", noticeService.getNoticeCount());
        return Result.success(data);
    }

    /** 作者端数据统计：我的应用数、总下载量、待审核数 */
    @GetMapping("/author")
    public Result<Map<String, Object>> getAuthorStatistics() {
        Long authorId = UserContext.getUserId();
        Map<String, Object> data = new HashMap<>();
        data.put("myAppCount", appInfoService.getAuthorAppCount(authorId));
        data.put("myDownloadCount", appInfoService.getAuthorTotalDownloadCount(authorId));
        data.put("pendingAuditCount", appInfoService.getAuthorPendingAuditCount(authorId));
        return Result.success(data);
    }
}
