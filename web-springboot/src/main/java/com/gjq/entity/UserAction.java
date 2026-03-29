package com.gjq.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户行为记录实体类
 */
@Data
@TableName("user_action")
public class UserAction {
    /**
     * ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 景点ID
     */
    private Long itemId;

    /**
     * 行为类型(0:浏览 1:预约)
     */
    private Integer actionType;

    /**
     * 扩展数据(JSON格式)
     */
    private String extraData;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
} 