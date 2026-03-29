package com.gjq.vo.useraction;

import com.gjq.vo.item.ItemVO;
import com.gjq.vo.user.UserInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户行为视图对象
 */
@Data
@Schema(description = "用户行为视图对象")
public class UserActionVO {
    /**
     * ID
     */
    @Schema(description = "ID")
    private Long id;

    /**
     * 用户ID
     */
    @Schema(description = "用户ID")
    private Long userId;

    /**
     * 用户信息
     */
    @Schema(description = "用户信息")
    private UserInfo userInfo;

    /**
     * 物品ID
     */
    @Schema(description = "物品ID")
    private Long itemId;

    /**
     * 物品信息
     */
    @Schema(description = "物品信息")
    private ItemVO item;

    /**
     * 行为类型(0:浏览 1:购买)
     */
    @Schema(description = "行为类型：0-浏览，1-购买")
    private Integer actionType;

    /**
     * 扩展数据(JSON格式)
     */
    @Schema(description = "扩展数据(JSON格式)")
    private String extraData;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
} 