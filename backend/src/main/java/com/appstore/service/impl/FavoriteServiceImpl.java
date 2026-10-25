package com.appstore.service.impl;

import com.appstore.common.PageResult;
import com.appstore.common.ResultCode;
import com.appstore.entity.AppInfo;
import com.appstore.entity.Favorite;
import com.appstore.exception.BusinessException;
import com.appstore.mapper.AppInfoMapper;
import com.appstore.mapper.FavoriteMapper;
import com.appstore.service.FavoriteService;
import com.appstore.util.UserContext;
import com.appstore.vo.AppInfoVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 收藏服务实现
 */
@Service
public class FavoriteServiceImpl implements FavoriteService {

    @Resource
    private FavoriteMapper favoriteMapper;

    @Resource
    private AppInfoMapper appInfoMapper;

    @Override
    public void addFavorite(Long appId) {
        Long userId = UserContext.getUserId();
        // 校验应用存在
        AppInfo app = appInfoMapper.selectById(appId);
        if (app == null) {
            throw new BusinessException(ResultCode.APP_NOT_EXIST);
        }
        Favorite exist = favoriteMapper.selectOne(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId)
                .eq(Favorite::getAppId, appId));
        // 已收藏则直接返回，避免重复数据（数据库唯一索引兜底）
        if (exist == null) {
            Favorite favorite = new Favorite();
            favorite.setUserId(userId);
            favorite.setAppId(appId);
            favoriteMapper.insert(favorite);
        }
    }

    @Override
    public void cancelFavorite(Long appId) {
        Long userId = UserContext.getUserId();
        favoriteMapper.delete(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId)
                .eq(Favorite::getAppId, appId));
    }

    @Override
    public boolean isFavorite(Long appId) {
        Long userId = UserContext.getUserId();
        if (userId == null) return false;
        Long count = favoriteMapper.selectCount(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId)
                .eq(Favorite::getAppId, appId));
        return count > 0;
    }

    @Override
    public PageResult<AppInfoVO> getMyFavorites(Integer pageNum, Integer pageSize) {
        Long userId = UserContext.getUserId();
        int page = (pageNum == null || pageNum < 1) ? 1 : pageNum;
        int size = (pageSize == null || pageSize < 1) ? 10 : Math.min(pageSize, 100);
        // SQL层分页：JOIN 应用表按收藏时间倒序，避免全量查询后内存分页
        Page<AppInfo> pageQuery = new Page<>(page, size);
        IPage<AppInfo> result = favoriteMapper.selectMyFavoriteApps(pageQuery, userId);
        List<AppInfoVO> voList = result.getRecords().stream().map(app -> {
            AppInfoVO vo = new AppInfoVO();
            vo.setId(app.getId());
            vo.setAppName(app.getAppName());
            vo.setAppIcon(app.getAppIcon());
            vo.setShortDesc(app.getShortDesc());
            vo.setVersion(app.getVersion());
            vo.setDownloadCount(app.getDownloadCount());
            vo.setRating(app.getRating());
            return vo;
        }).collect(Collectors.toList());
        return new PageResult<>(result.getTotal(), voList, page, size);
    }

    @Override
    public long getFavoriteCount() {
        return favoriteMapper.selectCount(null);
    }
}
