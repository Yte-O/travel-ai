package com.gjq.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gjq.common.Result;
import com.gjq.dto.comment.CommentAddDTO;
import com.gjq.dto.comment.CommentQueryDTO;
import com.gjq.entity.Comment;
import com.gjq.entity.Item;
import com.gjq.mapper.CommentMapper;
import com.gjq.mapper.ItemMapper;
import com.gjq.service.CommentService;
import com.gjq.utils.SecurityUtils;
import com.gjq.vo.comment.CommentVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 评论控制器
 */
@RestController
@RequestMapping("/comment")
public class CommentController {

    @Autowired
    private CommentService commentService;
    
    @Autowired
    private CommentMapper commentMapper;
    
    @Autowired
    private ItemMapper itemMapper;

    /**
     * 添加评论
     *
     * @param commentAddDTO 评论添加DTO
     * @return 结果
     */
    @PostMapping
    public Result<Long> addComment(@RequestBody @Valid CommentAddDTO commentAddDTO) {
        Long userId = SecurityUtils.getUserId();
        Long commentId = commentService.addComment(userId, commentAddDTO);
        return Result.success(commentId);
    }

    /**
     * 删除评论
     *
     * @param commentId 评论ID
     * @return 结果
     */
    @DeleteMapping("/{commentId}")
    public Result<?> deleteComment(@PathVariable Long commentId) {
        // 获取当前用户ID
        Long userId = SecurityUtils.getUserId();
        
        // 获取评论信息
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            return Result.error("评论不存在");
        }
        
        // 获取物品创建者ID
        Long itemCreatorId = null;
        if (comment.getItemId() != null) {
            Item item = itemMapper.selectById(comment.getItemId());
            if (item != null) {
                itemCreatorId = item.getUserId();
            }
        }
        
        // 执行带权限检查的删除
        try {
            boolean success = commentService.deleteCommentWithPermissionCheck(userId, commentId, SecurityUtils.isAdmin(), itemCreatorId);
            return success ? Result.success() : Result.error("删除失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取评论详情
     *
     * @param commentId 评论ID
     * @return 评论VO
     */
    @GetMapping("/{commentId}")
    public Result<CommentVO> getComment(@PathVariable Long commentId) {
        CommentVO commentVO = commentService.getCommentById(commentId);
        return Result.success(commentVO);
    }

    /**
     * 分页查询评论
     *
     * @param queryDTO 查询条件
     * @return 评论VO分页
     */
    @GetMapping("/page")
    public Result<Page<CommentVO>> pageComments(CommentQueryDTO queryDTO) {
        Page<CommentVO> page = commentService.pageComments(queryDTO);
        return Result.success(page);
    }

    /**
     * 获取物品评论树
     *
     * @param itemId 物品ID
     * @return 评论VO列表（树形结构）
     */
    @GetMapping("/tree/{itemId}")
    public Result<List<CommentVO>> getCommentTree(@PathVariable Long itemId) {
        List<CommentVO> commentTree = commentService.getCommentTreeByItemId(itemId);
        return Result.success(commentTree);
    }
} 