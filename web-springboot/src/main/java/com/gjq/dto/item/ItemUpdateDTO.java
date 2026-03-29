package com.gjq.dto.item;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 更新物品DTO
 */
@Data
@Schema(description = "更新物品DTO")
public class ItemUpdateDTO {

    @Schema(description = "物品ID", required = true)
    @NotNull(message = "物品ID不能为空")
    private Long id;

    @Schema(description = "物品标题")
    @Size(max = 100, message = "物品标题长度不能超过100个字符")
    private String title;

    @Schema(description = "物品描述")
    private String description;

    @Schema(description = "类别ID")
    private Long categoryId;

    @Schema(description = "封面图片存储桶")
    private String coverBucket;

    @Schema(description = "封面图片对象键")
    private String coverObjectKey;

    @Schema(description = "文件存储桶")
    private String fileBucket;

    @Schema(description = "文件对象键")
    private String fileObjectKey;

    @Schema(description = "标签，使用逗号分隔")
    @Size(max = 255, message = "标签长度不能超过255个字符")
    private String tags;

    @Schema(description = "额外数据，JSON格式，用于存储未来可能添加的字段")
    private String extraData;
} 