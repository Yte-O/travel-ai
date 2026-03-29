package com.gjq.dto;

import lombok.Data;

/**
 * 分页查询基础DTO
 */
@Data
public class PageDTO {
    /**
     * 当前页码
     */
    private Integer current = 1;

    /**
     * 每页大小
     */
    private Integer size = 10;
} 