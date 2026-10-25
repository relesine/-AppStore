package com.appstore.service;

import com.appstore.entity.AppVersion;
import java.util.List;

public interface AppVersionService {
    /** 查询应用版本列表（status为空返回全部，可指定status过滤） */
    List<AppVersion> getVersionsByAppId(Long appId, Integer status);
    void addVersion(AppVersion version);
    void updateVersion(AppVersion version);
    void deleteVersion(Long id);
}
