package com.appstore.mapper;

import com.appstore.entity.Comment;
import com.appstore.vo.CommentVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {

    @Select("SELECT c.*, u.username, u.nickname, u.avatar " +
            "FROM app_comment c LEFT JOIN sys_user u ON c.user_id = u.id " +
            "WHERE c.app_id = #{appId} AND c.deleted = 0 AND c.status = 1 " +
            "ORDER BY c.create_time DESC")
    List<CommentVO> selectByAppId(@Param("appId") Long appId);
}
