package com.gjq.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gjq.client.FileClient;
import com.gjq.common.Result;
import com.gjq.dto.item.ItemAddDTO;
import com.gjq.dto.item.ItemQueryDTO;
import com.gjq.dto.item.ItemUpdateDTO;
import com.gjq.service.ItemService;
import com.gjq.utils.SecurityUtils;
import com.gjq.vo.item.ItemVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 景点控制器
 */
@RestController
@RequestMapping("/item")
@Tag(name = "景点管理", description = "景点相关接口")
public class ItemController {

    @Autowired
    private ItemService itemService;

    @Autowired
    private FileClient fileClient;

    /**
     * 添加景点
     */
    @PostMapping
    @Operation(summary = "添加景点", description = "添加景点信息")
    public Result<Long> add(@RequestBody ItemAddDTO dto) {
        // 校验权限
        if (!SecurityUtils.isAdmin()) {
            return Result.error("权限不足");
        }
        
        // 设置创建者为当前登录用户
        dto.setUserId(SecurityUtils.getUserId());
        
        Long id = itemService.add(dto);
        return Result.success(id);
    }

    /**
     * 更新景点
     */
    @PutMapping
    @Operation(summary = "更新景点", description = "更新景点信息")
    public Result<Void> update(@RequestBody ItemUpdateDTO dto) {
        itemService.update(dto);
        return Result.success();
    }

    /**
     * 删除景点
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除景点", description = "根据ID删除景点")
    public Result<Void> delete(@Parameter(description = "景点ID", required = true) @PathVariable Long id) {
        // 校验权限
        if (!SecurityUtils.isAdmin()) {
            return Result.error("权限不足");
        }
        
        itemService.delete(id);
        return Result.success();
    }

    /**
     * 获取景点详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取景点详情", description = "根据ID获取景点详情")
    public Result<ItemVO> getById(@Parameter(description = "景点ID", required = true) @PathVariable Long id) {
        ItemVO vo = itemService.getById(id);
        return Result.success(vo);
    }

    /**
     * 分页查询景点
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询景点", description = "分页查询景点信息")
    public Result<Page<ItemVO>> page(ItemQueryDTO dto) {
        Page<ItemVO> page = itemService.page(dto);
        return Result.success(page);
    }

    /**
     * 根据类别ID获取景点列表
     */
    @GetMapping("/list/category/{categoryId}")
    @Operation(summary = "根据类别ID获取景点列表", description = "根据类别ID获取景点列表")
    public Result<List<ItemVO>> listByCategoryId(
            @Parameter(description = "类别ID", required = true) @PathVariable Long categoryId) {
        List<ItemVO> list = itemService.listByCategoryId(categoryId);
        return Result.success(list);
    }

    /**
     * 根据标签获取景点列表
     */
    @GetMapping("/list/tag/{tag}")
    @Operation(summary = "根据标签获取景点列表", description = "根据标签获取景点列表")
    public Result<List<ItemVO>> listByTag(
            @Parameter(description = "标签", required = true) @PathVariable String tag) {
        List<ItemVO> list = itemService.listByTag(tag);
        return Result.success(list);
    }
} 