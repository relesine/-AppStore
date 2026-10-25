package com.appstore.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("app_comment")
public class Comment {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long appId;
    private Long userId;
    private String content;
    private Integer rating;
    private Long parentId;
    private Integer likeCount;
    private Integer status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
