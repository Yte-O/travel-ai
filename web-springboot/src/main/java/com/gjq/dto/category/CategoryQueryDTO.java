package com.gjq.dto.category;

import com.gjq.dto.PageDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 查询类别DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "查询类别DTO")
public class CategoryQueryDTO extends PageDTO {

    @Schema(description = "类别名称")
    private String name;

    @Schema(description = "类别描述")
    private String description;
} 