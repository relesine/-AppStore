package com.appstore.controller;

import com.appstore.common.PageResult;
import com.appstore.common.Result;
import com.appstore.entity.AppInfo;
import com.appstore.service.AppInfoService;
import com.appstore.vo.AppInfoVO;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/app")
public class AppController {

    @Resource
    private AppInfoService appInfoService;

    @GetMapping("/list")
    public Result<PageResult<AppInfoVO>> getAppList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long subCategoryId,
            @RequestParam(required = false) String keyword) {
        return Result.success(appInfoService.getAppList(pageNum, pageSize, categoryId, subCategoryId, keyword));
    }

    @GetMapping("/detail/{id}")
    public Result<AppInfoVO> getAppDetail(@PathVariable Long id) {
        return Result.success(appInfoService.getAppDetail(id));
    }

    @GetMapping("/hot")
    public Result<List<AppInfoVO>> getHotApps(@RequestParam(defaultValue = "6") Integer limit) {
        return Result.success(appInfoService.getHotApps(limit));
    }

    @GetMapping("/new")
    public Result<List<AppInfoVO>> getNewApps(@RequestParam(defaultValue = "6") Integer limit) {
        return Result.success(appInfoService.getNewApps(limit));
    }

    @GetMapping("/search")
    public Result<PageResult<AppInfoVO>> search(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam String keyword) {
        return Result.success(appInfoService.getAppList(pageNum, pageSize, null, null, keyword));
    }

    @PostMapping("/add")
    public Result<Void> addApp(@RequestBody AppInfo appInfo) {
        appInfoService.addApp(appInfo);
        return Result.success();
    }

    @PutMapping("/update")
    public Result<Void> updateApp(@RequestBody AppInfo appInfo) {
        appInfoService.updateApp(appInfo);
        return Result.success();
    }

    @DeleteMapping("/delete/{id}")
    public Result<Void> deleteApp(@PathVariable Long id) {
        appInfoService.deleteApp(id);
        return Result.success();
    }

    @GetMapping("/author/list")
    public Result<PageResult<AppInfoVO>> getAuthorApps(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(appInfoService.getAuthorApps(pageNum, pageSize));
    }

    @GetMapping("/audit/list")
    public Result<PageResult<AppInfoVO>> getAuditList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer auditStatus) {
        return Result.success(appInfoService.getAuditList(pageNum, pageSize, auditStatus));
    }
}
