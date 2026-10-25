package com.appstore.controller;

import com.appstore.common.PageResult;
import com.appstore.common.Result;
import com.appstore.entity.Notice;
import com.appstore.service.NoticeService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 公告控制器
 */
@RestController
@RequestMapping("/notice")
public class NoticeController {

    @Resource
    private NoticeService noticeService;

    /**
     * 分页查询公告
     * @param status 状态过滤：null-已发布(默认)，0-草稿，1-已发布，-1-全部（管理端用）
     */
    @GetMapping("/list")
    public Result<PageResult<Notice>> getNoticeList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer noticeType,
            @RequestParam(required = false) Integer status) {
        return Result.success(noticeService.getNoticeList(pageNum, pageSize, noticeType, status));
    }

    @GetMapping("/detail/{id}")
    public Result<Notice> getNoticeDetail(@PathVariable Long id) {
        return Result.success(noticeService.getNoticeDetail(id));
    }

    @PostMapping("/add")
    public Result<Void> addNotice(@RequestBody Notice notice) {
        noticeService.addNotice(notice);
        return Result.success();
    }

    @PutMapping("/update")
    public Result<Void> updateNotice(@RequestBody Notice notice) {
        noticeService.updateNotice(notice);
        return Result.success();
    }

    @DeleteMapping("/delete/{id}")
    public Result<Void> deleteNotice(@PathVariable Long id) {
        noticeService.deleteNotice(id);
        return Result.success();
    }
}
