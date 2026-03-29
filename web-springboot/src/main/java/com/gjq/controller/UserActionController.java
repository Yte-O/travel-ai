package com.gjq.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gjq.common.Result;
import com.gjq.dto.useraction.UserActionDTO;
import com.gjq.dto.useraction.UserActionQueryDTO;
import com.gjq.service.UserActionService;
import com.gjq.utils.SecurityUtils;
import com.gjq.vo.useraction.UserActionVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 用户行为控制器
 */
@Tag(name = "用户行为", description = "用户行为相关接口")
@RestController
@RequestMapping("/user-action")
public class UserActionController {

    @Autowired
    private UserActionService userActionService;

    /**
     * 添加用户行为记录
     *
     * @param dto 用户行为DTO
     * @return 是否成功
     */
    @Operation(summary = "添加用户行为记录", description = "添加用户浏览/预约记录")
    @PostMapping
    public Result<Boolean> addUserAction(@RequestBody @Valid UserActionDTO dto) {
        Long userId = SecurityUtils.getUserId();
        boolean success = userActionService.addUserAction(userId, dto);
        return Result.success(success);
    }

    /**
     * 分页查询当前用户的行为记录
     *
     * @param dto 查询条件
     * @return 分页结果
     */
    @Operation(summary = "分页查询当前用户的行为记录", description = "分页查询当前用户的浏览/预约记录")
    @GetMapping("/page")
    public Result<Page<UserActionVO>> pageMyActions(UserActionQueryDTO dto) {
        // 设置当前用户ID
        dto.setUserId(SecurityUtils.getUserId());
        Page<UserActionVO> page = userActionService.pageUserActions(dto);
        return Result.success(page);
    }

    /**
     * 分页查询所有用户的行为记录（管理员接口）
     *
     * @param dto 查询条件
     * @return 分页结果
     */
    @Operation(summary = "分页查询所有用户的行为记录", description = "分页查询所有用户的浏览/预约记录（管理员接口）")
    @GetMapping("/admin/page")
    public Result<Page<UserActionVO>> pageAllActions(UserActionQueryDTO dto) {
        // 检查权限
        if (!SecurityUtils.isAdmin()) {
            return Result.error("权限不足");
        }
        
        Page<UserActionVO> page = userActionService.pageUserActions(dto);
        return Result.success(page);
    }

    /**
     * 批量删除用户行为记录（管理员接口）
     *
     * @param ids 要删除的记录ID列表
     * @return 是否成功
     */
    @Operation(summary = "批量删除用户行为记录", description = "批量删除用户行为记录（管理员接口）")
    @DeleteMapping("/batch")
    public Result<Boolean> batchDeleteActions(@RequestBody @Valid @Parameter(description = "记录ID列表") 
                                             List<Long> ids) {
        // 检查权限
        if (!SecurityUtils.isAdmin()) {
            return Result.error("权限不足");
        }
        
        boolean success = userActionService.batchDeleteActions(ids);
        return Result.success(success);
    }
    
    /**
     * 批量删除当前用户的行为记录
     *
     * @param ids 要删除的记录ID列表
     * @return 是否成功
     */
    @Operation(summary = "批量删除当前用户的行为记录", description = "批量删除当前用户的行为记录")
    @DeleteMapping("/my/batch")
    public Result<Boolean> batchDeleteMyActions(@RequestBody @Valid @Parameter(description = "记录ID列表") 
                                              List<Long> ids) {
        Long userId = SecurityUtils.getUserId();
        boolean success = userActionService.batchDeleteMyActions(userId, ids);
        return Result.success(success);
    }
    
    /**
     * 获取景点的浏览数
     *
     * @param itemId 物品ID
     * @return 浏览数
     */
    @Operation(summary = "获取景点的浏览数", description = "获取指定景点的浏览数")
    @GetMapping("/view/count/{itemId}")
    public Result<Long> getViewCount(@PathVariable Long itemId) {
        // 浏览行为类型为0
        long count = userActionService.getActionCountByItemIdAndType(itemId, 0);
        return Result.success(count);
    }
    
    /**
     * 获取景点的预约数
     *
     * @param itemId 物品ID
     * @return 预约数
     */
    @Operation(summary = "获取景点的预约数", description = "获取指定景点的预约数")
    @GetMapping("/reservation/count/{itemId}")
    public Result<Long> getReservationCount(@PathVariable Long itemId) {
        // 预约行为类型为1
        long count = userActionService.getActionCountByItemIdAndType(itemId, 1);
        return Result.success(count);
    }
} 