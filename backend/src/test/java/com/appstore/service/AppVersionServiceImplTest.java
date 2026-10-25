package com.appstore.service;

import com.appstore.common.ResultCode;
import com.appstore.entity.AppInfo;
import com.appstore.entity.AppVersion;
import com.appstore.exception.BusinessException;
import com.appstore.mapper.AppInfoMapper;
import com.appstore.mapper.AppVersionMapper;
import com.appstore.service.impl.AppVersionServiceImpl;
import com.appstore.util.UserContext;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 版本服务单元测试
 * 覆盖：版本更新接口防审核绕过、版本列表未过审内容可见性收敛
 */
@ExtendWith(MockitoExtension.class)
class AppVersionServiceImplTest {

    @Mock
    private AppVersionMapper appVersionMapper;

    @Mock
    private AppInfoMapper appInfoMapper;

    @InjectMocks
    private AppVersionServiceImpl appVersionService;

    @BeforeEach
    void setUp() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(UserContext.USER_ID, 2L);
        request.setAttribute(UserContext.USERNAME, "author1");
        request.setAttribute(UserContext.USER_TYPE, 2);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    // ---------- 版本更新防绕过 ----------

    @Test
    @DisplayName("更新版本时强制重置审核状态，防止作者直接置为已过审")
    void updateVersion_shouldResetAuditStatus() {
        AppVersion exist = new AppVersion();
        exist.setId(1L);
        exist.setAppId(5L);
        exist.setStatus(1);
        when(appVersionMapper.selectById(1L)).thenReturn(exist);

        AppInfo app = new AppInfo();
        app.setId(5L);
        app.setAuthorId(2L);
        when(appInfoMapper.selectById(5L)).thenReturn(app);

        // 恶意请求：伪造已过审状态与发布时间、版本编码
        AppVersion malicious = new AppVersion();
        malicious.setId(1L);
        malicious.setStatus(1);
        malicious.setPublishTime(LocalDateTime.now());
        malicious.setVersionCode(999);
        malicious.setAppId(999L);
        malicious.setUpdateLog("正常更新说明");

        appVersionService.updateVersion(malicious);

        ArgumentCaptor<AppVersion> captor = ArgumentCaptor.forClass(AppVersion.class);
        verify(appVersionMapper).updateById(captor.capture());
        AppVersion updated = captor.getValue();

        assertEquals(0, updated.getStatus(), "status 必须重置为待审核");
        assertNull(updated.getPublishTime(), "publishTime 必须清空");
        assertNull(updated.getVersionCode(), "versionCode 不允许修改");
        assertNull(updated.getAppId(), "appId 不允许修改");
        assertEquals("正常更新说明", updated.getUpdateLog());
    }

    @Test
    @DisplayName("非作者修改他人应用版本被拒绝")
    void updateVersion_shouldRejectNonAuthor() {
        AppVersion exist = new AppVersion();
        exist.setId(1L);
        exist.setAppId(5L);
        when(appVersionMapper.selectById(1L)).thenReturn(exist);

        AppInfo app = new AppInfo();
        app.setId(5L);
        app.setAuthorId(99L); // 应用属于作者99
        when(appInfoMapper.selectById(5L)).thenReturn(app);

        AppVersion req = new AppVersion();
        req.setId(1L);

        BusinessException ex = assertThrows(BusinessException.class, () -> appVersionService.updateVersion(req));
        assertEquals(ResultCode.FORBIDDEN.getCode(), ex.getCode());
        verify(appVersionMapper, never()).updateById(any(AppVersion.class));
    }

    // ---------- 版本列表可见性 ----------

    @Test
    @DisplayName("未登录游客查询全部版本（含未过审）被拒绝")
    void getVersions_shouldRejectPublicAllStatus() {
        RequestContextHolder.resetRequestAttributes(); // 游客

        AppInfo app = new AppInfo();
        app.setId(5L);
        app.setAuthorId(2L);
        when(appInfoMapper.selectById(5L)).thenReturn(app);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> appVersionService.getVersionsByAppId(5L, null));
        assertEquals(ResultCode.FORBIDDEN.getCode(), ex.getCode());
    }

    @Test
    @DisplayName("游客查询已过审版本（status=1）允许")
    void getVersions_shouldAllowPublicApproved() {
        RequestContextHolder.resetRequestAttributes(); // 游客

        when(appVersionMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.singletonList(new AppVersion()));

        List<AppVersion> list = appVersionService.getVersionsByAppId(5L, 1);
        assertNotNull(list);
        assertEquals(1, list.size());
    }

    @Test
    @DisplayName("应用作者可查询自己应用的全部版本")
    void getVersions_shouldAllowOwnAuthor() {
        AppInfo app = new AppInfo();
        app.setId(5L);
        app.setAuthorId(2L); // 与当前作者一致
        when(appInfoMapper.selectById(5L)).thenReturn(app);
        when(appVersionMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());

        List<AppVersion> list = appVersionService.getVersionsByAppId(5L, null);
        assertNotNull(list);
    }
}
