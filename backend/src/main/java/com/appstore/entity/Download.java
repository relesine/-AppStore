package com.appstore.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("app_download")
public class Download {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long appId;
    private Long versionId;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime downloadTime;
    private String ip;
}
