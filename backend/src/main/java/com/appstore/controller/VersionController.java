package com.appstore.controller;

import com.appstore.common.Result;
import com.appstore.entity.AppVersion;
import com.appstore.service.AppVersionService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 应用版本控制器
 */
@RestController
@RequestMapping("/version")
public class VersionController {

    @Resource
    private AppVersionService appVersionService;

    /**
     * 查询应用版本列表
     * @param appId 应用ID
     * @param status 可选，状态过滤（1=已通过，用户端传入仅看已上架版本）
     */
    @GetMapping("/list/{appId}")
    public Result<List<AppVersion>> getVersions(@PathVariable Long appId,
                                                @RequestParam(required = false) Integer status) {
        return Result.success(appVersionService.getVersionsByAppId(appId, status));
    }

    @PostMapping("/add")
    public Result<Void> addVersion(@RequestBody AppVersion version) {
        appVersionService.addVersion(version);
        return Result.success();
    }

    @PutMapping("/update")
    public Result<Void> updateVersion(@RequestBody AppVersion version) {
        appVersionService.updateVersion(version);
        return Result.success();
    }

    @DeleteMapping("/delete/{id}")
    public Result<Void> deleteVersion(@PathVariable Long id) {
        appVersionService.deleteVersion(id);
        return Result.success();
    }
}
