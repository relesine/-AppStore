package com.appstore.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("app_audit")
public class Audit {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long appId;
    private Long versionId;
    private Long auditorId;
    private Integer auditType;
    private Integer auditResult;
    private String auditRemark;
    private LocalDateTime auditTime;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
