package com.appstore.service.impl;

import com.appstore.common.PageResult;
import com.appstore.entity.Notice;
import com.appstore.mapper.NoticeMapper;
import com.appstore.service.NoticeService;
import com.appstore.util.UserContext;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;

/**
 * 公告服务实现
 */
@Service
public class NoticeServiceImpl implements NoticeService {

    @Resource
    private NoticeMapper noticeMapper;

    @Override
    public PageResult<Notice> getNoticeList(Integer pageNum, Integer pageSize, Integer noticeType, Integer status) {
        int page = (pageNum == null || pageNum < 1) ? 1 : pageNum;
        int size = (pageSize == null || pageSize < 1) ? 10 : Math.min(pageSize, 100);
        Page<Notice> pageQuery = new Page<>(page, size);
        LambdaQueryWrapper<Notice> wrapper = new LambdaQueryWrapper<Notice>()
                .eq(Notice::getDeleted, 0)
                .orderByDesc(Notice::getPublishTime);
        // 安全加固：status=0(草稿)/-1(全部) 仅管理员可查看，普通用户只能看已发布公告，
        // 防止通过拼接参数获取未发布草稿内容
        Integer currentUserType = com.appstore.util.UserContext.getUserType();
        boolean isAdmin = currentUserType != null && currentUserType == 3;
        if (status == null) {
            wrapper.eq(Notice::getStatus, 1);
        } else if (status == -1 || status == 0) {
            if (!isAdmin) {
                wrapper.eq(Notice::getStatus, 1);
            }
        } else {
            // 非管理员一律只返回已发布，防止枚举 status 越权
            if (isAdmin) {
                wrapper.eq(Notice::getStatus, status);
            } else {
                wrapper.eq(Notice::getStatus, 1);
            }
        }
        if (noticeType != null) {
            wrapper.eq(Notice::getNoticeType, noticeType);
        }
        Page<Notice> result = noticeMapper.selectPage(pageQuery, wrapper);
        return new PageResult<>(result.getTotal(), result.getRecords(), page, size);
    }

    @Override
    public Notice getNoticeDetail(Long id) {
        Notice notice = noticeMapper.selectById(id);
        if (notice == null) {
            return null;
        }
        // 安全加固：草稿仅管理员可查看详情
        if (notice.getStatus() != null && notice.getStatus() == 0) {
            Integer currentUserType = com.appstore.util.UserContext.getUserType();
            if (currentUserType == null || currentUserType != 3) {
                return null;
            }
        }
        return notice;
    }

    @Override
    public void addNotice(Notice notice) {
        if (notice.getTitle() == null || notice.getTitle().trim().isEmpty()) {
            throw new com.appstore.exception.BusinessException(
                    com.appstore.common.ResultCode.PARAM_ERROR.getCode(), "公告标题不能为空");
        }
        if (notice.getTitle().length() > 100) {
            throw new com.appstore.exception.BusinessException(
                    com.appstore.common.ResultCode.PARAM_ERROR.getCode(), "公告标题不能超过100字");
        }
        // XSS防护：公告内容可能存在 HTML 标签，入库前统一清洗
        notice.setTitle(com.appstore.util.XssUtil.clean(notice.getTitle().trim()));
        if (notice.getContent() != null) {
            notice.setContent(com.appstore.util.XssUtil.clean(notice.getContent()));
        }
        notice.setPublisherId(com.appstore.util.UserContext.getUserId());
        if (notice.getStatus() == null) {
            notice.setStatus(1);
        }
        if (notice.getStatus() == 1 && notice.getPublishTime() == null) {
            notice.setPublishTime(LocalDateTime.now());
        }
        noticeMapper.insert(notice);
    }

    @Override
    public void updateNotice(Notice notice) {
        if (notice.getId() == null) {
            throw new com.appstore.exception.BusinessException(
                    com.appstore.common.ResultCode.PARAM_ERROR.getCode(), "公告ID不能为空");
        }
        // 标题与内容XSS清洗
        if (notice.getTitle() != null) {
            if (notice.getTitle().trim().isEmpty() || notice.getTitle().length() > 100) {
                throw new com.appstore.exception.BusinessException(
                        com.appstore.common.ResultCode.PARAM_ERROR.getCode(), "公告标题需在1-100字之间");
            }
            notice.setTitle(com.appstore.util.XssUtil.clean(notice.getTitle().trim()));
        }
        if (notice.getContent() != null) {
            notice.setContent(com.appstore.util.XssUtil.clean(notice.getContent()));
        }
        // 由草稿改为发布时自动补齐发布时间
        if (notice.getStatus() != null && notice.getStatus() == 1) {
            Notice exist = noticeMapper.selectById(notice.getId());
            if ((exist == null || exist.getPublishTime() == null) && notice.getPublishTime() == null) {
                notice.setPublishTime(LocalDateTime.now());
            }
        }
        noticeMapper.updateById(notice);
    }

    @Override
    public void deleteNotice(Long id) {
        noticeMapper.deleteById(id);
    }

    @Override
    public long getNoticeCount() {
        return noticeMapper.selectCount(new LambdaQueryWrapper<Notice>().eq(Notice::getDeleted, 0));
    }
}
