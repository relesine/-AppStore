package com.appstore.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CommentVO {
    private Long id;
    private Long appId;
    private Long userId;
    private String username;
    private String nickname;
    private String avatar;
    private String content;
    private Integer rating;
    private Long parentId;
    private Integer likeCount;
    private LocalDateTime createTime;
}
