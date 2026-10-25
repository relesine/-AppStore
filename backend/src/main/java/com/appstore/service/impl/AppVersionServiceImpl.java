package com.appstore.service.impl;

import com.appstore.common.ResultCode;
import com.appstore.entity.AppInfo;
import com.appstore.entity.AppVersion;
import com.appstore.exception.BusinessException;
import com.appstore.mapper.AppInfoMapper;
import com.appstore.mapper.AppVersionMapper;
import com.appstore.service.AppVersionService;
import com.appstore.util.UserContext;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 应用版本服务实现
 */
@Slf4j
@Service
public class AppVersionServiceImpl implements AppVersionService {

    @Resource
    private AppVersionMapper appVersionMapper;

    @Resource
    private AppInfoMapper appInfoMapper;

    @Override
    public List<AppVersion> getVersionsByAppId(Long appId, Integer status) {
        // 安全加固：status 为空（查询全部版本，含待审核/已拒绝）时，
        // 仅允许应用作者本人或管理员查看，防止普通用户获取未过审版本的更新日志与文件地址
        if (status == null) {
            AppInfo app = appInfoMapper.selectById(appId);
            Long userId = UserContext.getUserId();
            Integer userType = UserContext.getUserType();
            boolean isAuthor = app != null && userId != null && app.getAuthorId().equals(userId);
            boolean isAdmin = userType != null && userType == 3;
            if (!isAuthor && !isAdmin) {
                throw new BusinessException(ResultCode.FORBIDDEN);
            }
        }
        LambdaQueryWrapper<AppVersion> wrapper = new LambdaQueryWrapper<AppVersion>()
                .eq(AppVersion::getAppId, appId)
                .eq(AppVersion::getDeleted, 0)
                .orderByDesc(AppVersion::getVersionCode);
        if (status != null) {
            wrapper.eq(AppVersion::getStatus, status);
        }
        return appVersionMapper.selectList(wrapper);
    }

    @Override
    public void addVersion(AppVersion version) {
        // 参数校验
        if (version.getAppId() == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "应用ID不能为空");
        }
        if (version.getVersionNo() == null || version.getVersionNo().trim().isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "版本号不能为空");
        }
        if (version.getFileUrl() == null || version.getFileUrl().trim().isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "请先上传安装包");
        }
        // 更新日志长度校验（与数据库字段保持一致）
        if (version.getUpdateLog() != null && version.getUpdateLog().length() > 500) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "更新日志不能超过500字");
        }
        // 校验应用存在且归属当前作者（管理员可代传）
        AppInfo app = appInfoMapper.selectById(version.getAppId());
        if (app == null) {
            throw new BusinessException(ResultCode.APP_NOT_EXIST);
        }
        Long userId = UserContext.getUserId();
        Integer userType = UserContext.getUserType();
        if (userType != 3 && !app.getAuthorId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        // 版本号格式校验：x.y.z 或 x.y
        if (!version.getVersionNo().matches("^\\d+(\\.\\d+){1,3}$")) {
            throw new BusinessException(ResultCode.VERSION_ERROR.getCode(), "版本号格式不正确（如 1.0.1）");
        }
        // 自动计算versionCode：取当前最大版本编码+1（避免逻辑删除导致count错乱）
        AppVersion maxVersion = appVersionMapper.selectOne(new LambdaQueryWrapper<AppVersion>()
                .eq(AppVersion::getAppId, version.getAppId())
                .orderByDesc(AppVersion::getVersionCode)
                .last("LIMIT 1"));
        int nextCode = (maxVersion != null && maxVersion.getVersionCode() != null)
                ? maxVersion.getVersionCode() + 1 : 1;
        version.setVersionCode(nextCode);
        version.setStatus(0); // 待审核
        version.setFileUrl(XssUtilClean(version.getFileUrl()));
        appVersionMapper.insert(version);
        log.info("作者 {} 提交新版本: appId={}, versionNo={}", userId, version.getAppId(), version.getVersionNo());
    }

    /**
     * 清洗文件URL（仅保留合法路径，防止注入）
     */
    private String XssUtilClean(String url) {
        if (url == null) {
            return null;
        }
        // 仅允许 /uploads/ 开头的站内路径
        if (url.startsWith("/uploads/")) {
            return url;
        }
        throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "安装包地址不合法");
    }

    @Override
    public void updateVersion(AppVersion version) {
        if (version.getId() == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "版本ID不能为空");
        }
        AppVersion exist = appVersionMapper.selectById(version.getId());
        if (exist == null) {
            throw new BusinessException(ResultCode.VERSION_ERROR.getCode(), "版本不存在");
        }
        // 归属校验：版本所属应用必须属于当前作者（管理员除外）
        AppInfo app = appInfoMapper.selectById(exist.getAppId());
        Long userId = UserContext.getUserId();
        Integer userType = UserContext.getUserType();
        if (app != null && userType != 3 && !app.getAuthorId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        // 安全加固（字段白名单）：
        // 1. 不允许修改所属应用、版本编码、审核状态、发布时间
        // 2. 修改文件或更新日志后强制重置为待审核，防止绕过版本审核直接上架
        version.setAppId(null);
        version.setVersionCode(null);
        version.setStatus(0);
        version.setPublishTime(null);
        version.setCreateTime(null);
        version.setUpdateTime(null);
        // 更新日志长度校验（与数据库字段保持一致）
        if (version.getUpdateLog() != null && version.getUpdateLog().length() > 500) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "更新日志不能超过500字");
        }
        appVersionMapper.updateById(version);
        log.info("更新版本: id={}, 操作人={}", version.getId(), UserContext.getUsername());
    }

    @Override
    public void deleteVersion(Long id) {
        AppVersion exist = appVersionMapper.selectById(id);
        if (exist == null) {
            throw new BusinessException(ResultCode.VERSION_ERROR.getCode(), "版本不存在");
        }
        // 归属校验：版本所属应用必须属于当前作者（管理员除外）
        AppInfo app = appInfoMapper.selectById(exist.getAppId());
        Long userId = UserContext.getUserId();
        Integer userType = UserContext.getUserType();
        if (app != null && userType != 3 && !app.getAuthorId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        appVersionMapper.deleteById(id);
        log.info("删除版本: id={}, 操作人={}", id, UserContext.getUsername());
    }
}
