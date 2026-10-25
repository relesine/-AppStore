package com.appstore.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("app_info")
public class AppInfo {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String appName;
    private String appIcon;
    private Long categoryId;
    private Long subCategoryId;
    private Long authorId;
    private String description;
    private String shortDesc;
    private String version;
    private Long size;
    private Integer downloadCount;
    private BigDecimal rating;
    private Integer ratingCount;
    private Integer status;
    private Integer auditStatus;
    private String screenshots;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
