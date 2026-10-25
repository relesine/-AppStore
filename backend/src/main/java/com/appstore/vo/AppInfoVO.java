package com.appstore.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AppInfoVO {
    private Long id;
    private String appName;
    private String appIcon;
    private Long categoryId;
    private String categoryName;
    private Long subCategoryId;
    private String subCategoryName;
    private Long authorId;
    private String authorName;
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
    private LocalDateTime createTime;
}
