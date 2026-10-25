package com.appstore.service;

import com.appstore.common.PageResult;
import com.appstore.vo.AppInfoVO;

public interface DownloadService {
    String downloadApp(Long appId, String ip);
    PageResult<AppInfoVO> getMyDownloads(Integer pageNum, Integer pageSize);
    long getDownloadCount();
    long getTodayDownloadCount();
}
