package com.gjq.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 通用景点实体类
 */
@Data
@TableName("item")
public class Item {
    /**
     * 景点ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 景点名称
     */
    private String title;

    /**
     * 景点描述
     */
    private String description;

    /**
     * 类别ID
     */
    private Long categoryId;

    /**
     * 用户ID（创建者）
     */
    private Long userId;

    /**
     * 封面图片存储桶
     */
    private String coverBucket;

    /**
     * 封面图片对象键
     */
    private String coverObjectKey;

    /**
     * 文件存储桶（用于存储视频、音频或其他文件）
     */
    private String fileBucket;

    /**
     * 文件对象键（用于存储视频、音频或其他文件）
     */
    private String fileObjectKey;

    /**
     * 标签，使用逗号分隔
     */
    private String tags;

    /**
     * 额外数据，JSON格式，用于存储未来可能添加的字段
     */
    private String extraData;

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