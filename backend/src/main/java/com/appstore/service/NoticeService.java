package com.appstore.service;

import com.appstore.common.PageResult;
import com.appstore.entity.Notice;

public interface NoticeService {
    /** 分页查询公告列表；status为空返回已发布，0返回草稿，-1返回全部（管理端） */
    PageResult<Notice> getNoticeList(Integer pageNum, Integer pageSize, Integer noticeType, Integer status);
    Notice getNoticeDetail(Long id);
    void addNotice(Notice notice);
    void updateNotice(Notice notice);
    void deleteNotice(Long id);
    long getNoticeCount();
}
