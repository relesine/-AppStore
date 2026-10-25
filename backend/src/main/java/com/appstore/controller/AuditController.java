package com.appstore.controller;

import com.appstore.common.Result;
import com.appstore.common.ResultCode;
import com.appstore.exception.BusinessException;
import com.appstore.service.AuditService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;

/**
 * 审核控制器（仅管理员）
 */
@RestController
@RequestMapping("/audit")
public class AuditController {

    @Resource
    private AuditService auditService;

    /** 应用上架审核（auditResult: 2=通过 3=拒绝） */
    @PostMapping("/app")
    public Result<Void> auditApp(@RequestBody Map<String, Object> params) {
        Object appIdObj = params.get("appId");
        Object auditResultObj = params.get("auditResult");
        if (appIdObj == null || auditResultObj == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "参数不完整");
        }
        Long appId = Long.valueOf(appIdObj.toString());
        Integer auditResult = Integer.valueOf(auditResultObj.toString());
        String auditRemark = params.get("auditRemark") != null ? params.get("auditRemark").toString() : "";
        auditService.auditApp(appId, auditResult, auditRemark);
        return Result.success();
    }

    /** 版本审核（auditResult: 2=通过 3=拒绝） */
    @PostMapping("/version")
    public Result<Void> auditVersion(@RequestBody Map<String, Object> params) {
        Object versionIdObj = params.get("versionId");
        Object auditResultObj = params.get("auditResult");
        if (versionIdObj == null || auditResultObj == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "参数不完整");
        }
        Long versionId = Long.valueOf(versionIdObj.toString());
        Integer auditResult = Integer.valueOf(auditResultObj.toString());
        String auditRemark = params.get("auditRemark") != null ? params.get("auditRemark").toString() : "";
        auditService.auditVersion(versionId, auditResult, auditRemark);
        return Result.success();
    }
}
