package com.gjq.dto.item;

import com.gjq.dto.PageDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 查询物品DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "查询物品DTO")
public class ItemQueryDTO extends PageDTO {

    @Schema(description = "物品标题")
    private String title;

    @Schema(description = "物品描述")
    private String description;

    @Schema(description = "类别ID")
    private Long categoryId;

    @Schema(description = "标签")
    private String tag;
    
    @Schema(description = "发布者真实姓名")
    private String userRealName;
} 