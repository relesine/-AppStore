package com.appstore.service;

import com.appstore.common.ResultCode;
import com.appstore.entity.AppInfo;
import com.appstore.exception.BusinessException;
import com.appstore.mapper.AppInfoMapper;
import com.appstore.mapper.CategoryMapper;
import com.appstore.service.impl.AppInfoServiceImpl;
import com.appstore.util.RedisUtil;
import com.appstore.util.UserContext;
import com.appstore.vo.AppInfoVO;
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

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 应用服务单元测试
 * 覆盖：更新接口字段白名单（防刷下载量/评分/绕过审核）、详情可见性收敛
 */
@ExtendWith(MockitoExtension.class)
class AppInfoServiceImplTest {

    @Mock
    private AppInfoMapper appInfoMapper;

    @Mock
    private CategoryMapper categoryMapper;

    @Mock
    private RedisUtil redisUtil;

    @InjectMocks
    private AppInfoServiceImpl appInfoService;

    @BeforeEach
    void setUp() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        // 默认模拟作者身份（userId=2, type=2）
        request.setAttribute(UserContext.USER_ID, 2L);
        request.setAttribute(UserContext.USERNAME, "author1");
        request.setAttribute(UserContext.USER_TYPE, 2);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    // ---------- 更新接口字段白名单 ----------

    @Test
    @DisplayName("更新应用时强制清除下载量/评分等敏感字段并重置审核状态")
    void updateApp_shouldClearSensitiveFields() {
        AppInfo exist = new AppInfo();
        exist.setId(1L);
        exist.setAuthorId(2L);
        when(appInfoMapper.selectById(1L)).thenReturn(exist);

        // 恶意请求：伪造下载量、评分、已上架状态
        AppInfo malicious = new AppInfo();
        malicious.setId(1L);
        malicious.setAppName("正常改名");
        malicious.setDownloadCount(99999);
        malicious.setRating(BigDecimal.valueOf(5.0));
        malicious.setRatingCount(999);
        malicious.setStatus(1);
        malicious.setAuditStatus(2);
        malicious.setSize(1024L);
        malicious.setVersion("9.9.9");

        appInfoService.updateApp(malicious);

        ArgumentCaptor<AppInfo> captor = ArgumentCaptor.forClass(AppInfo.class);
        verify(appInfoMapper).updateById(captor.capture());
        AppInfo updated = captor.getValue();

        // 敏感字段必须被置空，防止作者刷数据
        assertNull(updated.getDownloadCount(), "downloadCount 必须被置空");
        assertNull(updated.getRating(), "rating 必须被置空");
        assertNull(updated.getRatingCount(), "ratingCount 必须被置空");
        assertNull(updated.getSize(), "size 必须被置空");
        assertNull(updated.getVersion(), "version 必须被置空");
        // 修改后强制待审核
        assertEquals(0, updated.getStatus(), "status 必须重置为待上架");
        assertEquals(0, updated.getAuditStatus(), "auditStatus 必须重置为待审核");
        // 允许的正常字段保留
        assertEquals("正常改名", updated.getAppName());
    }

    @Test
    @DisplayName("非作者修改他人应用被拒绝")
    void updateApp_shouldRejectNonAuthor() {
        AppInfo exist = new AppInfo();
        exist.setId(1L);
        exist.setAuthorId(99L); // 应用属于作者99，当前用户是2
        when(appInfoMapper.selectById(1L)).thenReturn(exist);

        AppInfo req = new AppInfo();
        req.setId(1L);
        req.setAppName("越权改名");

        BusinessException ex = assertThrows(BusinessException.class, () -> appInfoService.updateApp(req));
        assertEquals(ResultCode.FORBIDDEN.getCode(), ex.getCode());
        verify(appInfoMapper, never()).updateById(any(AppInfo.class));
    }

    // ---------- 详情可见性收敛 ----------

    @Test
    @DisplayName("未上架应用详情对普通游客不可见")
    void getAppDetail_shouldHidePendingAppFromPublic() {
        // 模拟无登录态（无请求上下文 → 游客）
        RequestContextHolder.resetRequestAttributes();

        AppInfoVO pending = new AppInfoVO();
        pending.setId(1L);
        pending.setStatus(0);
        pending.setAuditStatus(0);
        pending.setAuthorId(2L);
        when(appInfoMapper.selectAppDetail(1L)).thenReturn(pending);

        BusinessException ex = assertThrows(BusinessException.class, () -> appInfoService.getAppDetail(1L));
        assertEquals(ResultCode.APP_NOT_EXIST.getCode(), ex.getCode());
    }

    @Test
    @DisplayName("应用作者本人可查看自己未上架应用详情")
    void getAppDetail_shouldAllowOwnAuthor() {
        AppInfoVO pending = new AppInfoVO();
        pending.setId(1L);
        pending.setStatus(0);
        pending.setAuditStatus(0);
        pending.setAuthorId(2L); // 与当前登录作者一致
        when(appInfoMapper.selectAppDetail(1L)).thenReturn(pending);

        AppInfoVO vo = appInfoService.getAppDetail(1L);
        assertNotNull(vo);
        assertEquals(1L, vo.getId());
    }

    @Test
    @DisplayName("管理员可查看任意应用详情（含未上架）")
    void getAppDetail_shouldAllowAdmin() {
        // 切换为管理员身份
        RequestContextHolder.resetRequestAttributes();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(UserContext.USER_ID, 1L);
        request.setAttribute(UserContext.USER_TYPE, 3);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        AppInfoVO pending = new AppInfoVO();
        pending.setId(1L);
        pending.setStatus(0);
        pending.setAuditStatus(0);
        pending.setAuthorId(2L);
        when(appInfoMapper.selectAppDetail(1L)).thenReturn(pending);

        AppInfoVO vo = appInfoService.getAppDetail(1L);
        assertNotNull(vo);
    }

    @Test
    @DisplayName("已上架应用详情对所有人可见")
    void getAppDetail_shouldBePublicWhenPublished() {
        RequestContextHolder.resetRequestAttributes(); // 游客

        AppInfoVO published = new AppInfoVO();
        published.setId(1L);
        published.setStatus(1);
        published.setAuditStatus(2);
        published.setAuthorId(2L);
        when(appInfoMapper.selectAppDetail(1L)).thenReturn(published);

        AppInfoVO vo = appInfoService.getAppDetail(1L);
        assertNotNull(vo);
    }
}
