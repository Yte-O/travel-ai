package com.gjq.dto.category;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 添加类别DTO
 */
@Data
@Schema(description = "添加类别DTO")
public class CategoryAddDTO {

    @Schema(description = "类别名称", required = true)
    @NotBlank(message = "类别名称不能为空")
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