package com.appstore.service.impl;

import com.appstore.common.ResultCode;
import com.appstore.entity.AppInfo;
import com.appstore.entity.Comment;
import com.appstore.exception.BusinessException;
import com.appstore.mapper.AppInfoMapper;
import com.appstore.mapper.CommentMapper;
import com.appstore.service.CommentService;
import com.appstore.util.UserContext;
import com.appstore.util.XssUtil;
import com.appstore.vo.CommentVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * 评论服务实现
 */
@Slf4j
@Service
public class CommentServiceImpl implements CommentService {

    @Resource
    private CommentMapper commentMapper;

    @Resource
    private AppInfoMapper appInfoMapper;

    @Override
    public List<CommentVO> getCommentsByAppId(Long appId) {
        return commentMapper.selectByAppId(appId);
    }

    @Override
    @Transactional
    public void addComment(Comment comment) {
        // 参数校验
        if (comment.getAppId() == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "应用ID不能为空");
        }
        if (comment.getRating() == null || comment.getRating() < 1 || comment.getRating() > 5) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "评分必须在1-5星之间");
        }
        if (comment.getContent() == null || comment.getContent().trim().isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "评论内容不能为空");
        }
        if (comment.getContent().length() > 500) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "评论内容不能超过500字");
        }
        // 应用必须存在且已上架
        AppInfo app = appInfoMapper.selectById(comment.getAppId());
        if (app == null || app.getStatus() != 1) {
            throw new BusinessException(ResultCode.APP_NOT_EXIST);
        }

        // XSS防护：过滤评论内容中的危险脚本
        comment.setContent(XssUtil.clean(comment.getContent().trim()));
        comment.setUserId(UserContext.getUserId());
        comment.setStatus(1);
        comment.setLikeCount(0);
        commentMapper.insert(comment);

        // 原子更新应用评分：评分人数+1 且平均分实时重算（单条SQL完成，避免并发丢计数）
        appInfoMapper.incrementRating(comment.getAppId(), comment.getRating());
    }

    @Override
    public void deleteComment(Long id) {
        Comment comment = commentMapper.selectById(id);
        if (comment == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "评论不存在");
        }
        // 权限校验：仅评论作者本人或管理员可删除
        Long userId = UserContext.getUserId();
        Integer userType = UserContext.getUserType();
        if (userType != 3 && !comment.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        commentMapper.deleteById(id);
        log.info("删除评论: id={}, 操作人={}", id, UserContext.getUsername());
    }

    @Override
    public long getCommentCount() {
        return commentMapper.selectCount(new LambdaQueryWrapper<Comment>().eq(Comment::getDeleted, 0));
    }
}
