package com.gjq.vo.category;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 类别视图对象
 */
@Data
@Schema(description = "类别视图对象")
public class CategoryVO {

    @Schema(description = "类别ID")
    private Long id;

    @Schema(description = "类别名称")
    private String name;

    @Schema(description = "类别描述")
    private String description;

    @Schema(description = "图标存储桶")
    private String iconBucket;

    @Schema(description = "图标对象键")
    private String iconObjectKey;

    @Schema(description = "图标URL")
    private String iconUrl;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
} 