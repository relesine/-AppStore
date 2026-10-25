package com.appstore.service;

import com.appstore.entity.Notice;
import com.appstore.mapper.NoticeMapper;
import com.appstore.service.impl.NoticeServiceImpl;
import com.appstore.util.UserContext;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.builder.MapperBuilderAssistant;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 公告服务单元测试
 * 覆盖：公告草稿泄露防护、公告内容XSS清洗、草稿详情可见性
 */
@ExtendWith(MockitoExtension.class)
class NoticeServiceImplTest {

    @Mock
    private NoticeMapper noticeMapper;

    @InjectMocks
    private NoticeServiceImpl noticeService;

    @BeforeEach
    void setUp() {
        // 初始化 MyBatis-Plus Lambda 元数据缓存（纯单元测试环境下不会自动注册）
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), Notice.class);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(UserContext.USER_ID, 1L);
        request.setAttribute(UserContext.USER_TYPE, 1);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    // ---------- 草稿泄露防护 ----------

    @Test
    @DisplayName("普通用户传status=-1请求时，服务端强制只返回已发布公告")
    void getNoticeList_shouldForcePublishedForNonAdmin() {
        when(noticeMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(new Page<>());

        noticeService.getNoticeList(1, 10, null, -1);

        ArgumentCaptor<LambdaQueryWrapper<Notice>> captor = ArgumentCaptor.forClass(LambdaQueryWrapper.class);
        verify(noticeMapper).selectPage(any(Page.class), captor.capture());
        // 普通用户场景下查询条件必须包含 status = 1（仅已发布）
        LambdaQueryWrapper<Notice> wrapper = captor.getValue();
        wrapper.getSqlSegment(); // 触发条件参数解析（MP 参数映射为惰性填充）
        assertTrue(wrapper.getParamNameValuePairs().containsValue(1),
                "普通用户查询必须强制追加 status=1 条件");
    }

    @Test
    @DisplayName("管理员传status=-1可查询全部公告（含草稿）")
    void getNoticeList_shouldAllowAdminAllStatus() {
        RequestContextHolder.resetRequestAttributes();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(UserContext.USER_ID, 1L);
        request.setAttribute(UserContext.USER_TYPE, 3); // 管理员
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        when(noticeMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(new Page<>());

        noticeService.getNoticeList(1, 10, null, -1);

        ArgumentCaptor<LambdaQueryWrapper<Notice>> captor = ArgumentCaptor.forClass(LambdaQueryWrapper.class);
        verify(noticeMapper).selectPage(any(Page.class), captor.capture());
        // 管理员查询全部时不应追加 status 条件
        assertFalse(captor.getValue().getSqlSegment().contains("status"));
    }

    // ---------- 草稿详情可见性 ----------

    @Test
    @DisplayName("草稿公告详情对普通用户不可见（返回null）")
    void getNoticeDetail_shouldHideDraftFromUser() {
        Notice draft = new Notice();
        draft.setId(1L);
        draft.setStatus(0);
        when(noticeMapper.selectById(1L)).thenReturn(draft);

        assertNull(noticeService.getNoticeDetail(1L));
    }

    @Test
    @DisplayName("管理员可查看草稿公告详情")
    void getNoticeDetail_shouldAllowAdmin() {
        RequestContextHolder.resetRequestAttributes();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(UserContext.USER_ID, 1L);
        request.setAttribute(UserContext.USER_TYPE, 3);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        Notice draft = new Notice();
        draft.setId(1L);
        draft.setStatus(0);
        when(noticeMapper.selectById(1L)).thenReturn(draft);

        assertNotNull(noticeService.getNoticeDetail(1L));
    }

    // ---------- XSS清洗 ----------

    @Test
    @DisplayName("新增公告时内容中的危险脚本被清洗")
    void addNotice_shouldCleanXss() {
        Notice notice = new Notice();
        notice.setTitle("公告标题");
        notice.setContent("<script>alert('xss')</script>正常内容");
        notice.setStatus(1);

        noticeService.addNotice(notice);

        ArgumentCaptor<Notice> captor = ArgumentCaptor.forClass(Notice.class);
        verify(noticeMapper).insert(captor.capture());
        assertFalse(captor.getValue().getContent().contains("<script>"),
                "script标签必须被清洗");
        assertTrue(captor.getValue().getContent().contains("正常内容"));
    }
}
