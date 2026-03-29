package com.gjq.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 物品类别实体类
 */
@Data
@TableName("category")
public class Category {
    /**
     * 类别ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 类别名称
     */
    private String name;

    /**
     * 类别描述
     */
    private String description;

    /**
     * 图标存储桶
     */
    private String iconBucket;

    /**
     * 图标对象键
     */
    private String iconObjectKey;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
} 