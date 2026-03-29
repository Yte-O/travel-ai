package com.gjq.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gjq.client.FileClient;
import com.gjq.dto.comment.CommentAddDTO;
import com.gjq.dto.comment.CommentQueryDTO;
import com.gjq.entity.Comment;
import com.gjq.entity.User;
import com.gjq.mapper.CommentMapper;
import com.gjq.service.CommentService;
import com.gjq.service.UserService;
import com.gjq.vo.comment.CommentVO;
import com.gjq.vo.user.UserInfo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import com.gjq.common.exception.BusinessException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 评论Service实现
 */
@Service
@Slf4j
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {

    @Autowired
    private UserService userService;
    
    @Autowired
    private FileClient fileClient;

    @Override
    @Transactional
    public Long addComment(Long userId, CommentAddDTO commentAddDTO) {
        Comment comment = new Comment();
        comment.setUserId(userId);
        comment.setItemId(commentAddDTO.getItemId());
        comment.setContent(commentAddDTO.getContent());
        comment.setParentId(commentAddDTO.getParentId());
        comment.setReplyToCommentId(commentAddDTO.getReplyToCommentId());
        comment.setReplyToUserId(commentAddDTO.getReplyToUserId());
        
        // 保存评论
        save(comment);
        
        return comment.getId();
    }

    @Override
    @Transactional
    public boolean deleteCommentWithPermissionCheck(Long userId, Long commentId, boolean isAdmin, Long itemCreatorId) {
        // 检查评论是否存在
        Comment comment = getById(commentId);
        if (comment == null) {
            throw new BusinessException("评论不存在");
        }
        
        // 权限检查
        boolean hasPermission = false;
        
        // 1. 管理员可以删除任何评论
        if (isAdmin) {
            hasPermission = true;
        } 
        // 2. 物品发布者可以删除自己物品下的所有评论
        else if (itemCreatorId != null && itemCreatorId.equals(userId) && 
                 comment.getItemId() != null) {
            hasPermission = true;
        } 
        // 3. 普通用户只能删除自己发布的评论
        else if (comment.getUserId().equals(userId)) {
            hasPermission = true;
        }
        
        if (!hasPermission) {
            throw new BusinessException("你没有权限删除此评论");
        }
        
        // 如果是顶级评论，删除所有回复
        if (comment.getParentId() == null) {
            remove(new LambdaQueryWrapper<Comment>().eq(Comment::getParentId, commentId));
        }
        
        // 删除评论
        return removeById(commentId);
    }

    @Override
    public CommentVO getCommentById(Long commentId) {
        Comment comment = getById(commentId);
        if (comment == null) {
            return null;
        }
        
        return convertToVO(comment);
    }

    @Override
    public Page<CommentVO> pageComments(CommentQueryDTO queryDTO) {
        // 构建查询条件
        LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();
        
        // 如果指定了物品ID，则查询该物品下的评论
        if (queryDTO.getItemId() != null) {
            queryWrapper.eq(Comment::getItemId, queryDTO.getItemId());
        }
        
        // 如果指定了用户ID，则查询该用户的评论
        if (queryDTO.getUserId() != null) {
            queryWrapper.eq(Comment::getUserId, queryDTO.getUserId());
        }
        
        // 如果只查询顶级评论
        if (queryDTO.getOnlyParent() != null && queryDTO.getOnlyParent()) {
            queryWrapper.isNull(Comment::getParentId);
        }
        
        // 按时间倒序
        queryWrapper.orderByDesc(Comment::getCreateTime);
        
        // 分页查询
        Page<Comment> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        page = page(page, queryWrapper);
        
        // 转换为VO
        Page<CommentVO> voPage = new Page<>();
        BeanUtils.copyProperties(page, voPage, "records");
        
        // 获取所有评论用户ID
        List<Long> userIds = page.getRecords().stream()
                .map(Comment::getUserId)
                .distinct()
                .collect(Collectors.toList());
        
        // 添加被回复用户ID
        page.getRecords().stream()
                .filter(comment -> comment.getReplyToUserId() != null)
                .map(Comment::getReplyToUserId)
                .distinct()
                .forEach(userIds::add);
        
        // 批量查询用户信息
        Map<Long, UserInfo> userMap = getUserMap(userIds);
        
        // 转换记录
        List<CommentVO> commentVOList = page.getRecords().stream()
                .map(comment -> {
                    CommentVO vo = convertToVO(comment);
                    // 设置用户信息
                    vo.setUserInfo(userMap.get(comment.getUserId()));
                    // 设置被回复用户信息
                    if (comment.getReplyToUserId() != null) {
                        vo.setReplyToUserInfo(userMap.get(comment.getReplyToUserId()));
                    }
                    return vo;
                })
                .collect(Collectors.toList());
        
        voPage.setRecords(commentVOList);
        
        return voPage;
    }

    @Override
    public List<CommentVO> getCommentTreeByItemId(Long itemId) {
        // 查询该物品的所有评论
        LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Comment::getItemId, itemId);
        queryWrapper.orderByDesc(Comment::getCreateTime);
        List<Comment> comments = list(queryWrapper);
        
        if (comments.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 获取所有评论用户ID
        List<Long> userIds = comments.stream()
                .map(Comment::getUserId)
                .distinct()
                .collect(Collectors.toList());
        
        // 添加被回复用户ID
        comments.stream()
                .filter(comment -> comment.getReplyToUserId() != null)
                .map(Comment::getReplyToUserId)
                .distinct()
                .forEach(userIds::add);
        
        // 批量查询用户信息
        Map<Long, UserInfo> userMap = getUserMap(userIds);
        
        // 转换为VO
        List<CommentVO> commentVOList = comments.stream()
                .map(comment -> {
                    CommentVO vo = convertToVO(comment);
                    // 设置用户信息
                    vo.setUserInfo(userMap.get(comment.getUserId()));
                    // 设置被回复用户信息
                    if (comment.getReplyToUserId() != null) {
                        vo.setReplyToUserInfo(userMap.get(comment.getReplyToUserId()));
                    }
                    return vo;
                })
                .collect(Collectors.toList());
        
        // 构建评论树
        return buildCommentTree(commentVOList);
    }
    
    /**
     * 将评论转换为VO
     */
    private CommentVO convertToVO(Comment comment) {
        CommentVO vo = new CommentVO();
        BeanUtils.copyProperties(comment, vo);
        return vo;
    }
    
    /**
     * 构建评论树
     */
    private List<CommentVO> buildCommentTree(List<CommentVO> commentList) {
        // 顶级评论
        List<CommentVO> rootComments = commentList.stream()
                .filter(comment -> comment.getParentId() == null)
                .collect(Collectors.toList());
        
        // 回复评论
        Map<Long, List<CommentVO>> replyMap = commentList.stream()
                .filter(comment -> comment.getParentId() != null)
                .collect(Collectors.groupingBy(CommentVO::getParentId));
        
        // 组装树形结构
        rootComments.forEach(root -> {
            List<CommentVO> replies = replyMap.get(root.getId());
            if (replies != null) {
                // 按时间排序，最新的回复在最下面
                replies.sort((a, b) -> a.getCreateTime().compareTo(b.getCreateTime()));
                root.setReplies(replies);
            } else {
                root.setReplies(new ArrayList<>());
            }
        });
        
        return rootComments;
    }
    
    /**
     * 批量获取用户信息
     */
    private Map<Long, UserInfo> getUserMap(List<Long> userIds) {
        if (userIds.isEmpty()) {
            return new HashMap<>();
        }
        
        // 批量查询用户
        List<User> users = userService.listByIds(userIds);
        
        // 转换为UserInfo
        Map<Long, UserInfo> userMap = new HashMap<>();
        users.forEach(user -> {
            UserInfo userInfo = new UserInfo(user, fileClient);
            userMap.put(user.getId(), userInfo);
        });
        
        return userMap;
    }
} 