package com.appstore.service.impl;

import com.appstore.common.ResultCode;
import com.appstore.entity.AppInfo;
import com.appstore.entity.AppVersion;
import com.appstore.entity.Audit;
import com.appstore.exception.BusinessException;
import com.appstore.mapper.AppInfoMapper;
import com.appstore.mapper.AppVersionMapper;
import com.appstore.mapper.AuditMapper;
import com.appstore.service.AuditService;
import com.appstore.util.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;

/**
 * 审核服务实现
 * 语义约定：
 * - 接口入参 auditResult：2=通过，3=拒绝（与 app_info.audit_status 一致）
 * - 审核日志表 app_audit.audit_result 存储：1=通过，2=拒绝（与数据库注释一致）
 */
@Slf4j
@Service
public class AuditServiceImpl implements AuditService {

    @Resource
    private AuditMapper auditMapper;

    @Resource
    private AppInfoMapper appInfoMapper;

    @Resource
    private AppVersionMapper appVersionMapper;

    @Override
    @Transactional
    public void auditApp(Long appId, Integer auditResult, String auditRemark) {
        if (appId == null || auditResult == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "参数不完整");
        }
        if (auditResult != 2 && auditResult != 3) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "审核结果不合法");
        }
        AppInfo app = appInfoMapper.selectById(appId);
        if (app == null) {
            throw new BusinessException(ResultCode.APP_NOT_EXIST);
        }
        // 仅待审核(0)/审核中(1)的应用可审核，防止重复审核
        if (app.getAuditStatus() != 0 && app.getAuditStatus() != 1) {
            throw new BusinessException(ResultCode.AUDIT_ERROR.getCode(), "该应用已审核，请勿重复操作");
        }

        // 更新应用审核状态
        app.setAuditStatus(auditResult);
        if (auditResult == 2) {
            app.setStatus(1); // 通过→上架
        } else if (auditResult == 3) {
            app.setStatus(2); // 拒绝→下架
        }
        appInfoMapper.updateById(app);

        // 记录审核日志（audit_result 存 1=通过 2=拒绝）
        Audit audit = new Audit();
        audit.setAppId(appId);
        audit.setAuditorId(UserContext.getUserId());
        audit.setAuditType(1);
        audit.setAuditResult(auditResult == 2 ? 1 : 2);
        audit.setAuditRemark(auditRemark);
        audit.setAuditTime(LocalDateTime.now());
        auditMapper.insert(audit);
        log.info("应用审核: appId={}, result={}, 审核人={}", appId, auditResult, UserContext.getUsername());
    }

    @Override
    @Transactional
    public void auditVersion(Long versionId, Integer auditResult, String auditRemark) {
        if (versionId == null || auditResult == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "参数不完整");
        }
        if (auditResult != 2 && auditResult != 3) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "审核结果不合法");
        }
        AppVersion version = appVersionMapper.selectById(versionId);
        if (version == null) {
            throw new BusinessException(ResultCode.VERSION_ERROR);
        }
        // 仅待审核(0)的版本可审核
        if (version.getStatus() != 0) {
            throw new BusinessException(ResultCode.AUDIT_ERROR.getCode(), "该版本已审核，请勿重复操作");
        }

        version.setStatus(auditResult == 2 ? 1 : 2);
        if (auditResult == 2) {
            version.setPublishTime(LocalDateTime.now());
            // 同步更新应用当前版本号
            AppInfo app = appInfoMapper.selectById(version.getAppId());
            if (app != null) {
                app.setVersion(version.getVersionNo());
                app.setSize(version.getFileSize());
                appInfoMapper.updateById(app);
            }
        }
        appVersionMapper.updateById(version);

        // 记录审核日志（audit_result 存 1=通过 2=拒绝）
        Audit audit = new Audit();
        audit.setAppId(version.getAppId());
        audit.setVersionId(versionId);
        audit.setAuditorId(UserContext.getUserId());
        audit.setAuditType(2);
        audit.setAuditResult(auditResult == 2 ? 1 : 2);
        audit.setAuditRemark(auditRemark);
        audit.setAuditTime(LocalDateTime.now());
        auditMapper.insert(audit);
        log.info("版本审核: versionId={}, result={}, 审核人={}", versionId, auditResult, UserContext.getUsername());
    }
}
