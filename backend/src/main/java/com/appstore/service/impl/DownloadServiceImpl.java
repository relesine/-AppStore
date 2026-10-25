package com.appstore.service.impl;

import com.appstore.common.PageResult;
import com.appstore.common.ResultCode;
import com.appstore.entity.AppInfo;
import com.appstore.entity.AppVersion;
import com.appstore.entity.Download;
import com.appstore.exception.BusinessException;
import com.appstore.mapper.AppInfoMapper;
import com.appstore.mapper.AppVersionMapper;
import com.appstore.mapper.DownloadMapper;
import com.appstore.service.DownloadService;
import com.appstore.util.RedisUtil;
import com.appstore.util.UserContext;
import com.appstore.vo.AppInfoVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 下载服务实现
 */
@Slf4j
@Service
public class DownloadServiceImpl implements DownloadService {

    @Resource
    private DownloadMapper downloadMapper;

    @Resource
    private AppInfoMapper appInfoMapper;

    @Resource
    private AppVersionMapper appVersionMapper;

    @Resource
    private RedisUtil redisUtil;

    @Override
    @Transactional
    public String downloadApp(Long appId, String ip) {
        AppInfo app = appInfoMapper.selectById(appId);
        if (app == null || app.getStatus() != 1) {
            throw new BusinessException(ResultCode.APP_NOT_EXIST);
        }

        // 防刷：同一IP同一应用1分钟内只计一次下载量
        String redisKey = "download:limit:" + ip + ":" + appId;
        Long count = redisUtil.incrementWithExpire(redisKey, 1, 1, TimeUnit.MINUTES);
        if (count != null && count == 1) {
            // 原子更新下载次数，避免并发丢失计数
            appInfoMapper.incrementDownloadCount(appId);
        }

        // 查询最新已通过审核的版本（记录与返回URL共用一次查询）
        AppVersion version = getLatestApprovedVersion(appId);

        // 记录下载（登录用户）
        Long userId = UserContext.getUserId();
        if (userId != null) {
            Download download = new Download();
            download.setUserId(userId);
            download.setAppId(appId);
            download.setVersionId(version != null ? version.getId() : null);
            download.setIp(ip);
            downloadMapper.insert(download);
        }

        // 返回下载URL
        if (version == null || version.getFileUrl() == null || version.getFileUrl().isEmpty()) {
            throw new BusinessException(ResultCode.VERSION_ERROR.getCode(), "该应用暂无可用安装包");
        }
        return version.getFileUrl();
    }

    /**
     * 获取应用最新已通过审核的版本
     */
    private AppVersion getLatestApprovedVersion(Long appId) {
        return appVersionMapper.selectOne(new LambdaQueryWrapper<AppVersion>()
                .eq(AppVersion::getAppId, appId)
                .eq(AppVersion::getStatus, 1)
                .eq(AppVersion::getDeleted, 0)
                .orderByDesc(AppVersion::getVersionCode)
                .last("LIMIT 1"));
    }

    @Override
    public PageResult<AppInfoVO> getMyDownloads(Integer pageNum, Integer pageSize) {
        Long userId = UserContext.getUserId();
        int page = (pageNum == null || pageNum < 1) ? 1 : pageNum;
        int size = (pageSize == null || pageSize < 1) ? 10 : Math.min(pageSize, 100);
        // SQL层分页：按应用去重并保留最近一次下载时间，避免全量查询后内存分页
        Page<AppInfo> pageQuery = new Page<>(page, size);
        IPage<AppInfo> result = downloadMapper.selectMyDownloadApps(pageQuery, userId);
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
    public long getDownloadCount() {
        return downloadMapper.selectCount(null);
    }

    @Override
    public long getTodayDownloadCount() {
        LocalDateTime start = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime end = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        return downloadMapper.selectCount(new LambdaQueryWrapper<Download>()
                .between(Download::getDownloadTime, start, end));
    }
}
