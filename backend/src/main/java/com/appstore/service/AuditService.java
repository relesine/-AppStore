package com.appstore.service;

import com.appstore.entity.Audit;

public interface AuditService {
    void auditApp(Long appId, Integer auditResult, String auditRemark);
    void auditVersion(Long versionId, Integer auditResult, String auditRemark);
}
