package com.gjq.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gjq.client.FileClient;
import com.gjq.common.Result;
import com.gjq.dto.category.CategoryAddDTO;
import com.gjq.dto.category.CategoryQueryDTO;
import com.gjq.dto.category.CategoryUpdateDTO;
import com.gjq.service.CategoryService;
import com.gjq.utils.SecurityUtils;
import com.gjq.vo.category.CategoryVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 类别控制器
 */
@RestController
@RequestMapping("/category")
@Tag(name = "类别管理", description = "类别相关接口")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private FileClient fileClient;

    /**
     * 添加类别
     */
    @PostMapping
    @Operation(summary = "添加类别", description = "添加类别信息")
    public Result<Long> add(@RequestBody CategoryAddDTO dto) {
        // 校验权限
        if (!SecurityUtils.isAdmin()) {
            return Result.error("权限不足");
        }
        
        Long id = categoryService.add(dto);
        return Result.success(id);
    }

    /**
     * 更新类别
     */
    @PutMapping
    @Operation(summary = "更新类别", description = "更新类别信息")
    public Result<Void> update(@RequestBody CategoryUpdateDTO dto) {
        // 校验权限
        if (!SecurityUtils.isAdmin()) {
            return Result.error("权限不足");
        }
        
        categoryService.update(dto);
        return Result.success();
    }

    /**
     * 删除类别
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除类别", description = "根据ID删除类别")
    public Result<Void> delete(@Parameter(description = "类别ID", required = true) @PathVariable Long id) {
        // 校验权限
        if (!SecurityUtils.isAdmin()) {
            return Result.error("权限不足");
        }
        
        categoryService.delete(id);
        return Result.success();
    }

    /**
     * 获取类别详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取类别详情", description = "根据ID获取类别详情")
    public Result<CategoryVO> getById(@Parameter(description = "类别ID", required = true) @PathVariable Long id) {
        CategoryVO vo = categoryService.getById(id);
        return Result.success(vo);
    }

    /**
     * 获取类别列表
     */
    @GetMapping("/list")
    @Operation(summary = "获取类别列表", description = "获取所有类别列表")
    public Result<List<CategoryVO>> list() {
        List<CategoryVO> list = categoryService.list();
        return Result.success(list);
    }

    /**
     * 分页查询类别
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询类别", description = "分页查询类别信息")
    public Result<Page<CategoryVO>> page(CategoryQueryDTO dto) {
        Page<CategoryVO> page = categoryService.page(dto);
        return Result.success(page);
    }
} 