package com.gjq.vo.item;

import com.gjq.vo.category.CategoryVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 物品视图对象
 */
@Data
@Schema(description = "物品视图对象")
public class ItemVO {

    @Schema(description = "物品ID")
    private Long id;

    @Schema(description = "物品标题")
    private String title;

    @Schema(description = "物品描述")
    private String description;

    @Schema(description = "类别ID")
    private Long categoryId;

    @Schema(description = "类别信息")
    private CategoryVO category;

    @Schema(description = "用户ID（创建者）")
    private Long userId;

    @Schema(description = "发布者真实姓名")
    private String userRealName;

    @Schema(description = "封面图片存储桶")
    private String coverBucket;

    @Schema(description = "封面图片对象键")
    private String coverObjectKey;

    @Schema(description = "封面图片URL")
    private String coverUrl;

    @Schema(description = "文件存储桶")
    private String fileBucket;

    @Schema(description = "文件对象键")
    private String fileObjectKey;

    @Schema(description = "文件URL")
    private String fileUrl;

    @Schema(description = "标签，使用逗号分隔")
    private String tags;

    @Schema(description = "标签列表")
    private String[] tagList;

    @Schema(description = "额外数据，JSON格式，用于存储未来可能添加的字段")
    private String extraData;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
} 