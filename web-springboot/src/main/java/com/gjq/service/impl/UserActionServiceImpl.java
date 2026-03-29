package com.gjq.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gjq.client.FileClient;
import com.gjq.dto.useraction.UserActionDTO;
import com.gjq.dto.useraction.UserActionQueryDTO;
import com.gjq.entity.Item;
import com.gjq.entity.User;
import com.gjq.entity.UserAction;
import com.gjq.mapper.ItemMapper;
import com.gjq.mapper.UserActionMapper;
import com.gjq.mapper.UserMapper;
import com.gjq.service.CategoryService;
import com.gjq.service.ItemService;
import com.gjq.service.UserActionService;
import com.gjq.vo.item.ItemVO;
import com.gjq.vo.user.UserInfo;
import com.gjq.vo.useraction.UserActionVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户行为Service实现类
 */
@Service
public class UserActionServiceImpl extends ServiceImpl<UserActionMapper, UserAction> implements UserActionService {

    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private ItemMapper itemMapper;
    
    @Autowired
    private ItemService itemService;
    
    @Autowired
    private FileClient fileClient;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addUserAction(Long userId, UserActionDTO dto) {
        UserAction userAction = new UserAction();
        userAction.setUserId(userId);
        userAction.setItemId(dto.getItemId());
        userAction.setActionType(dto.getActionType());
        userAction.setExtraData(dto.getExtraData());
        
        // 保存用户行为记录
        return save(userAction);
    }

    @Override
    public Page<UserActionVO> pageUserActions(UserActionQueryDTO dto) {
        // 构建查询条件
        LambdaQueryWrapper<UserAction> queryWrapper = new LambdaQueryWrapper<>();
        
        // 处理用户名模糊查询
        if (StringUtils.hasText(dto.getUsername())) {
            List<User> users = userMapper.selectList(new LambdaQueryWrapper<User>()
                    .like(User::getUsername, dto.getUsername()));
            if (!users.isEmpty()) {
                List<Long> userIdList = users.stream().map(User::getId).toList();
                queryWrapper.in(UserAction::getUserId, userIdList);
            } else {
                // 如果没有匹配的用户，返回空结果
                return new Page<>();
            }
        } else if (dto.getUserId() != null) {
            // 如果指定了用户ID
            queryWrapper.eq(UserAction::getUserId, dto.getUserId());
        }
        
        // 处理景点名称模糊查询
        if (StringUtils.hasText(dto.getItemTitle())) {
            List<Item> items = itemMapper.selectList(new LambdaQueryWrapper<Item>()
                    .like(Item::getTitle, dto.getItemTitle()));
            if (!items.isEmpty()) {
                List<Long> itemIdList = items.stream().map(Item::getId).toList();
                queryWrapper.in(UserAction::getItemId, itemIdList);
            } else {
                // 如果没有匹配的景点，返回空结果
                return new Page<>();
            }
        } else if (dto.getItemId() != null) {
            // 如果指定了景点ID
            queryWrapper.eq(UserAction::getItemId, dto.getItemId());
        }
        
        // 如果指定了行为类型
        if (dto.getActionType() != null) {
            queryWrapper.eq(UserAction::getActionType, dto.getActionType());
        }
        
        // 按时间倒序排序
        queryWrapper.orderByDesc(UserAction::getCreateTime);
        
        // 分页查询
        Page<UserAction> page = new Page<>(dto.getCurrent(), dto.getSize());
        page = page(page, queryWrapper);
        
        // 转换为VO
        Page<UserActionVO> voPage = new Page<>();
        BeanUtils.copyProperties(page, voPage, "records");
        
        // 处理records
        if (page.getRecords() != null && !page.getRecords().isEmpty()) {
            // 获取所有用户ID
            List<Long> userIds = new ArrayList<>();
            // 获取所有景点ID
            List<Long> itemIds = new ArrayList<>();
            
            // 收集所有ID
            for (UserAction userAction : page.getRecords()) {
                if (userAction.getUserId() != null) {
                    userIds.add(userAction.getUserId());
                }
                if (userAction.getItemId() != null) {
                    itemIds.add(userAction.getItemId());
                }
            }
            
            // 批量查询用户信息
            Map<Long, User> userMap = new HashMap<>();
            if (!userIds.isEmpty()) {
                List<User> users = userMapper.selectBatchIds(userIds);
                users.forEach(user -> userMap.put(user.getId(), user));
            }
            
            // 批量查询景点信息
            Map<Long, Item> itemMap = new HashMap<>();
            if (!itemIds.isEmpty()) {
                List<Item> items = itemMapper.selectBatchIds(itemIds);
                items.forEach(item -> itemMap.put(item.getId(), item));
            }
            
            // 转换成VO
            List<UserActionVO> voList = new ArrayList<>();
            for (UserAction userAction : page.getRecords()) {
                UserActionVO vo = toVO(userAction);
                
                // 设置用户信息
                User user = userMap.get(userAction.getUserId());
                if (user != null) {
                    vo.setUserInfo(new UserInfo(user, fileClient));
                }
                
                // 设置景点信息
                Item item = itemMap.get(userAction.getItemId());
                if (item != null) {
                    vo.setItem(itemService.toVO(item));
                }
                
                voList.add(vo);
            }
            
            voPage.setRecords(voList);
        }
        
        return voPage;
    }

    @Override
    public UserActionVO toVO(UserAction userAction) {
        if (userAction == null) {
            return null;
        }
        
        UserActionVO vo = new UserActionVO();
        BeanUtils.copyProperties(userAction, vo);
        return vo;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchDeleteActions(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return false;
        }
        
        // 批量删除记录
        return removeBatchByIds(ids);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchDeleteMyActions(Long userId, List<Long> ids) {
        if (userId == null || ids == null || ids.isEmpty()) {
            return false;
        }
        
        // 构建查询条件，确保只删除当前用户自己的记录
        LambdaQueryWrapper<UserAction> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserAction::getUserId, userId)
                    .in(UserAction::getId, ids);
        
        // 删除符合条件的记录
        return remove(queryWrapper);
    }
    
    @Override
    public long getActionCountByItemIdAndType(Long itemId, int actionType) {
        // 构建查询条件
        LambdaQueryWrapper<UserAction> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserAction::getItemId, itemId)
                   .eq(UserAction::getActionType, actionType);
        
        // 统计记录数量
        return count(queryWrapper);
    }
} 