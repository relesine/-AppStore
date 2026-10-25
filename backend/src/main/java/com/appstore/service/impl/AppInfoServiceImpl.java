package com.appstore.service.impl;

import com.appstore.common.PageResult;
import com.appstore.common.ResultCode;
import com.appstore.entity.AppInfo;
import com.appstore.exception.BusinessException;
import com.appstore.mapper.AppInfoMapper;
import com.appstore.service.AppInfoService;
import com.appstore.util.RedisUtil;
import com.appstore.util.UserContext;
import com.appstore.vo.AppInfoVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 应用信息服务实现
 */
@Slf4j
@Service
public class AppInfoServiceImpl implements AppInfoService {

    /** 热门应用Redis缓存key */
    private static final String HOT_APPS_CACHE_KEY = "appstore:hot:apps";

    /** 热门应用缓存过期时间（秒） */
    private static final long HOT_APPS_CACHE_TTL = 300L;

    @Resource
    private AppInfoMapper appInfoMapper;

    @Resource
    private RedisUtil redisUtil;

    @Override
    public PageResult<AppInfoVO> getAppList(Integer pageNum, Integer pageSize, Long categoryId, Long subCategoryId, String keyword) {
        // 参数防御：页码/页数非法时回退默认值
        int page = (pageNum == null || pageNum < 1) ? 1 : pageNum;
        int size = (pageSize == null || pageSize < 1) ? 10 : Math.min(pageSize, 100);
        Page<AppInfoVO> pageQuery = new Page<>(page, size);
        // SQL层分页，避免内存分页性能问题
        IPage<AppInfoVO> result = appInfoMapper.selectAppListPage(pageQuery, categoryId, subCategoryId, keyword);
        return new PageResult<>(result.getTotal(), result.getRecords(), page, size);
    }

    @Override
    public AppInfoVO getAppDetail(Long id) {
        AppInfoVO vo = appInfoMapper.selectAppDetail(id);
        if (vo == null) {
            throw new BusinessException(ResultCode.APP_NOT_EXIST);
        }
        // 安全加固：未上架应用（待审核/已拒绝/下架）仅允许作者本人或管理员查看详情，
        // 防止普通用户通过猜测ID获取未过审应用的信息（避免泄露审核中内容）
        if (vo.getStatus() == null || vo.getStatus() != 1 || vo.getAuditStatus() == null || vo.getAuditStatus() != 2) {
            Long userId = UserContext.getUserId();
            Integer userType = UserContext.getUserType();
            boolean isAuthor = userId != null && userId.equals(vo.getAuthorId());
            boolean isAdmin = userType != null && userType == 3;
            if (!isAuthor && !isAdmin) {
                throw new BusinessException(ResultCode.APP_NOT_EXIST);
            }
        }
        return vo;
    }

    @Override
    public List<AppInfoVO> getHotApps(Integer limit) {
        int size = (limit == null || limit < 1) ? 6 : Math.min(limit, 50);
        // 优先读Redis缓存
        try {
            Object cached = redisUtil.get(HOT_APPS_CACHE_KEY);
            if (cached instanceof List) {
                return (List<AppInfoVO>) cached;
            }
        } catch (Exception e) {
            // 缓存读取异常（如反序列化失败）时降级查询数据库，保证接口可用
            log.warn("热门应用读取缓存失败，降级查询数据库: {}", e.getMessage());
        }
        // 缓存未命中：查询数据库（仅已上架应用，按下载量倒序）
        List<AppInfoVO> list = appInfoMapper.selectHotApps(size);
        // 写入缓存，5分钟过期
        try {
            redisUtil.set(HOT_APPS_CACHE_KEY, list, HOT_APPS_CACHE_TTL, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("热门应用写入缓存失败: {}", e.getMessage());
        }
        return list;
    }

    @Override
    public List<AppInfoVO> getNewApps(Integer limit) {
        int size = (limit == null || limit < 1) ? 6 : Math.min(limit, 50);
        LambdaQueryWrapper<AppInfo> wrapper = new LambdaQueryWrapper<AppInfo>()
                .eq(AppInfo::getStatus, 1)
                .eq(AppInfo::getAuditStatus, 2)
                .eq(AppInfo::getDeleted, 0)
                .orderByDesc(AppInfo::getCreateTime)
                .last("LIMIT " + size);
        List<AppInfo> apps = appInfoMapper.selectList(wrapper);
        return apps.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public void addApp(AppInfo appInfo) {
        // 参数校验
        if (appInfo.getAppName() == null || appInfo.getAppName().trim().isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "应用名称不能为空");
        }
        if (appInfo.getCategoryId() == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "请选择应用分类");
        }
        Long authorId = UserContext.getUserId();
        appInfo.setAuthorId(authorId);
        appInfo.setStatus(0);
        appInfo.setAuditStatus(0);
        appInfo.setDownloadCount(0);
        appInfo.setRatingCount(0);
        if (appInfo.getRating() == null) {
            appInfo.setRating(new java.math.BigDecimal("0.0"));
        }
        appInfoMapper.insert(appInfo);
    }

    @Override
    public void updateApp(AppInfo appInfo) {
        if (appInfo.getId() == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "应用ID不能为空");
        }
        AppInfo exist = appInfoMapper.selectById(appInfo.getId());
        if (exist == null) {
            throw new BusinessException(ResultCode.APP_NOT_EXIST);
        }
        // 作者只能修改自己的应用，管理员可修改全部
        Long userId = UserContext.getUserId();
        Integer userType = UserContext.getUserType();
        if (userType != 3 && !exist.getAuthorId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        // 安全加固（字段白名单）：
        // 1. 不允许通过更新接口篡改作者、下载量、评分、版本、大小等业务敏感字段
        // 2. 修改后强制重新进入待审核状态，防止绕过审核直接上架
        appInfo.setAuthorId(null);
        appInfo.setDownloadCount(null);
        appInfo.setRating(null);
        appInfo.setRatingCount(null);
        appInfo.setVersion(null);
        appInfo.setSize(null);
        appInfo.setStatus(0);
        appInfo.setAuditStatus(0);
        appInfo.setCreateTime(null);
        appInfo.setUpdateTime(null);
        appInfoMapper.updateById(appInfo);
        log.info("更新应用: id={}, 操作人={}", appInfo.getId(), UserContext.getUsername());
    }

    @Override
    public void deleteApp(Long id) {
        AppInfo exist = appInfoMapper.selectById(id);
        if (exist == null) {
            throw new BusinessException(ResultCode.APP_NOT_EXIST);
        }
        // 权限校验：作者只能删除自己的应用，管理员可删除全部
        Long userId = UserContext.getUserId();
        Integer userType = UserContext.getUserType();
        if (userType != 3 && !exist.getAuthorId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        appInfoMapper.deleteById(id);
        log.info("删除应用: id={}, 操作人={}", id, UserContext.getUsername());
    }

    @Override
    public PageResult<AppInfoVO> getAuthorApps(Integer pageNum, Integer pageSize) {
        Long authorId = UserContext.getUserId();
        int page = (pageNum == null || pageNum < 1) ? 1 : pageNum;
        int size = (pageSize == null || pageSize < 1) ? 10 : Math.min(pageSize, 100);
        Page<AppInfo> pageQuery = new Page<>(page, size);
        LambdaQueryWrapper<AppInfo> wrapper = new LambdaQueryWrapper<AppInfo>()
                .eq(AppInfo::getAuthorId, authorId)
                .eq(AppInfo::getDeleted, 0)
                .orderByDesc(AppInfo::getCreateTime);
        Page<AppInfo> result = appInfoMapper.selectPage(pageQuery, wrapper);
        List<AppInfoVO> voList = result.getRecords().stream().map(this::convertToVO).collect(Collectors.toList());
        return new PageResult<>(result.getTotal(), voList, page, size);
    }

    @Override
    public PageResult<AppInfoVO> getAuditList(Integer pageNum, Integer pageSize, Integer auditStatus) {
        int page = (pageNum == null || pageNum < 1) ? 1 : pageNum;
        int size = (pageSize == null || pageSize < 1) ? 10 : Math.min(pageSize, 100);
        Page<AppInfo> pageQuery = new Page<>(page, size);
        LambdaQueryWrapper<AppInfo> wrapper = new LambdaQueryWrapper<AppInfo>()
                .eq(AppInfo::getDeleted, 0)
                .orderByDesc(AppInfo::getCreateTime);
        if (auditStatus != null) {
            wrapper.eq(AppInfo::getAuditStatus, auditStatus);
        }
        Page<AppInfo> result = appInfoMapper.selectPage(pageQuery, wrapper);
        List<AppInfoVO> voList = result.getRecords().stream().map(this::convertToVO).collect(Collectors.toList());
        return new PageResult<>(result.getTotal(), voList, page, size);
    }

    @Override
    public long getAppCount() {
        return appInfoMapper.selectCount(new LambdaQueryWrapper<AppInfo>().eq(AppInfo::getDeleted, 0));
    }

    @Override
    public long getPendingAuditCount() {
        return appInfoMapper.selectCount(new LambdaQueryWrapper<AppInfo>()
                .eq(AppInfo::getAuditStatus, 0)
                .eq(AppInfo::getDeleted, 0));
    }

    @Override
    public long getAuthorAppCount(Long authorId) {
        if (authorId == null) {
            return 0L;
        }
        return appInfoMapper.selectCount(new LambdaQueryWrapper<AppInfo>()
                .eq(AppInfo::getAuthorId, authorId)
                .eq(AppInfo::getDeleted, 0));
    }

    @Override
    public long getAuthorPendingAuditCount(Long authorId) {
        if (authorId == null) {
            return 0L;
        }
        return appInfoMapper.selectAuthorPendingCount(authorId);
    }

    @Override
    public long getAuthorTotalDownloadCount(Long authorId) {
        if (authorId == null) {
            return 0L;
        }
        Long total = appInfoMapper.selectAuthorTotalDownloads(authorId);
        return total != null ? total : 0L;
    }

    private AppInfoVO convertToVO(AppInfo app) {
        AppInfoVO vo = new AppInfoVO();
        vo.setId(app.getId());
        vo.setAppName(app.getAppName());
        vo.setAppIcon(app.getAppIcon());
        vo.setCategoryId(app.getCategoryId());
        vo.setSubCategoryId(app.getSubCategoryId());
        vo.setAuthorId(app.getAuthorId());
        vo.setDescription(app.getDescription());
        vo.setShortDesc(app.getShortDesc());
        vo.setVersion(app.getVersion());
        vo.setSize(app.getSize());
        vo.setDownloadCount(app.getDownloadCount());
        vo.setRating(app.getRating());
        vo.setRatingCount(app.getRatingCount());
        vo.setStatus(app.getStatus());
        vo.setAuditStatus(app.getAuditStatus());
        vo.setScreenshots(app.getScreenshots());
        vo.setCreateTime(app.getCreateTime());
        return vo;
    }
}
