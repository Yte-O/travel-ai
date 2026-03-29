package com.gjq.dto.category;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 更新类别DTO
 */
@Data
@Schema(description = "更新类别DTO")
public class CategoryUpdateDTO {

    @Schema(description = "类别ID", required = true)
    @NotNull(message = "类别ID不能为空")
    private Long id;

    @Schema(description = "类别名称")
    @Size(max = 50, message = "类别名称长度不能超过50个字符")
    private String name;

    @Schema(description = "类别描述")
    @Size(max = 255, message = "类别描述长度不能超过255个字符")
    private String description;

    @Schema(description = "图标存储桶")
    private String iconBucket;

    @Schema(description = "图标对象键")
    private String iconObjectKey;
} 