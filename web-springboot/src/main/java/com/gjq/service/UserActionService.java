package com.gjq.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gjq.dto.useraction.UserActionDTO;
import com.gjq.dto.useraction.UserActionQueryDTO;
import com.gjq.entity.UserAction;
import com.gjq.vo.useraction.UserActionVO;

import java.util.List;

/**
 * 用户行为Service接口
 */
public interface UserActionService extends IService<UserAction> {
    
    /**
     * 添加用户行为记录
     *
     * @param userId 用户ID
     * @param dto 用户行为DTO
     * @return 是否成功
     */
    boolean addUserAction(Long userId, UserActionDTO dto);
    
    /**
     * 分页查询用户行为
     *
     * @param dto 查询条件
     * @return 分页结果
     */
    Page<UserActionVO> pageUserActions(UserActionQueryDTO dto);
    
    /**
     * 将UserAction实体转换为VO
     *
     * @param userAction 用户行为实体
     * @return 用户行为VO
     */
    UserActionVO toVO(UserAction userAction);
    
    /**
     * 批量删除用户行为记录
     *
     * @param ids 要删除的记录ID列表
     * @return 是否成功
     */
    boolean batchDeleteActions(List<Long> ids);
    
    /**
     * 批量删除当前用户的行为记录
     *
     * @param userId 用户ID
     * @param ids 要删除的记录ID列表
     * @return 是否成功
     */
    boolean batchDeleteMyActions(Long userId, List<Long> ids);
    
    /**
     * 根据物品ID和行为类型统计行为数量
     *
     * @param itemId 物品ID
     * @param actionType 行为类型（0-浏览，1-购买）
     * @return 行为数量
     */
    long getActionCountByItemIdAndType(Long itemId, int actionType);
} 