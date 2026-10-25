package com.appstore.controller;

import com.appstore.common.PageResult;
import com.appstore.common.Result;
import com.appstore.service.FavoriteService;
import com.appstore.vo.AppInfoVO;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/favorite")
public class FavoriteController {

    @Resource
    private FavoriteService favoriteService;

    @PostMapping("/add/{appId}")
    public Result<Void> addFavorite(@PathVariable Long appId) {
        favoriteService.addFavorite(appId);
        return Result.success();
    }

    @DeleteMapping("/cancel/{appId}")
    public Result<Void> cancelFavorite(@PathVariable Long appId) {
        favoriteService.cancelFavorite(appId);
        return Result.success();
    }

    @GetMapping("/check/{appId}")
    public Result<Boolean> isFavorite(@PathVariable Long appId) {
        return Result.success(favoriteService.isFavorite(appId));
    }

    @GetMapping("/my")
    public Result<PageResult<AppInfoVO>> getMyFavorites(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(favoriteService.getMyFavorites(pageNum, pageSize));
    }
}
