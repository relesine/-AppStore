package com.appstore.service;

import com.appstore.entity.Comment;
import com.appstore.vo.CommentVO;
import java.util.List;

public interface CommentService {
    List<CommentVO> getCommentsByAppId(Long appId);
    void addComment(Comment comment);
    void deleteComment(Long id);
    long getCommentCount();
}
