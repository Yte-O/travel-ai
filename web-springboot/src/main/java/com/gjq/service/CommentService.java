package com.gjq.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gjq.dto.comment.CommentAddDTO;
import com.gjq.dto.comment.CommentQueryDTO;
import com.gjq.entity.Comment;
import com.gjq.vo.comment.CommentVO;

import java.util.List;

/**
 * 评论Service接口
 */
public interface CommentService extends IService<Comment> {
    
    /**
     * 添加评论
     *
     * @param userId 用户ID
     * @param commentAddDTO 评论添加DTO
     * @return 评论ID
     */
    Long addComment(Long userId, CommentAddDTO commentAddDTO);
    
    /**
     * 删除评论（带权限检查）
     * - 管理员可以删除任何评论
     * - 物品发布者可以删除自己物品下的所有评论
     * - 普通用户只能删除自己发布的评论
     *
     * @param userId 用户ID
     * @param commentId 评论ID
     * @param isAdmin 是否为管理员
     * @param itemCreatorId 物品创建者ID（如果当前用户是物品创建者则传入）
     * @return 是否成功
     */
    boolean deleteCommentWithPermissionCheck(Long userId, Long commentId, boolean isAdmin, Long itemCreatorId);
    
    /**
     * 获取评论详情
     *
     * @param commentId 评论ID
     * @return 评论VO
     */
    CommentVO getCommentById(Long commentId);
    
    /**
     * 分页查询评论
     *
     * @param queryDTO 查询条件
     * @return 评论VO分页
     */
    Page<CommentVO> pageComments(CommentQueryDTO queryDTO);
    
    /**
     * 根据物品ID获取评论树（顶级评论及其回复）
     *
     * @param itemId 物品ID
     * @return 评论VO列表（已构建成树形结构）
     */
    List<CommentVO> getCommentTreeByItemId(Long itemId);
} 