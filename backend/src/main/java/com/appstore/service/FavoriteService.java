package com.appstore.service;

import com.appstore.common.PageResult;
import com.appstore.vo.AppInfoVO;

public interface FavoriteService {
    void addFavorite(Long appId);
    void cancelFavorite(Long appId);
    boolean isFavorite(Long appId);
    PageResult<AppInfoVO> getMyFavorites(Integer pageNum, Integer pageSize);
    long getFavoriteCount();
}
