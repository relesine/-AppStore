package com.appstore.service;

import com.appstore.common.PageResult;
import com.appstore.entity.AppInfo;
import com.appstore.vo.AppInfoVO;
import java.util.List;

public interface AppInfoService {
    PageResult<AppInfoVO> getAppList(Integer pageNum, Integer pageSize, Long categoryId, Long subCategoryId, String keyword);
    AppInfoVO getAppDetail(Long id);
    List<AppInfoVO> getHotApps(Integer limit);
    List<AppInfoVO> getNewApps(Integer limit);
    void addApp(AppInfo appInfo);
    void updateApp(AppInfo appInfo);
    void deleteApp(Long id);
    PageResult<AppInfoVO> getAuthorApps(Integer pageNum, Integer pageSize);
    PageResult<AppInfoVO> getAuditList(Integer pageNum, Integer pageSize, Integer auditStatus);
    long getAppCount();
    long getPendingAuditCount();
    /** 统计指定作者的应用总数 */
    long getAuthorAppCount(Long authorId);
    /** 统计指定作者的待审核应用数 */
    long getAuthorPendingAuditCount(Long authorId);
    /** 统计指定作者的累计应用下载总量 */
    long getAuthorTotalDownloadCount(Long authorId);
}
