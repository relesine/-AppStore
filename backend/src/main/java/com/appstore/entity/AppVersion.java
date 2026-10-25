package com.appstore.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("app_version")
public class AppVersion {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long appId;
    private String versionNo;
    private Integer versionCode;
    private String fileUrl;
    private Long fileSize;
    private String fileMd5;
    private String updateLog;
    private Integer status;
    private LocalDateTime publishTime;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
