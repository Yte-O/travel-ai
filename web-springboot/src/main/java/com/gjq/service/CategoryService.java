package com.gjq.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gjq.dto.category.CategoryAddDTO;
import com.gjq.dto.category.CategoryQueryDTO;
import com.gjq.dto.category.CategoryUpdateDTO;
import com.gjq.entity.Category;
import com.gjq.vo.category.CategoryVO;

import java.util.List;

/**
 * 类别服务接口
 */
public interface CategoryService {

    /**
     * 添加类别
     *
     * @param dto 添加类别DTO
     * @return 类别ID
     */
    Long add(CategoryAddDTO dto);

    /**
     * 更新类别
     *
     * @param dto 更新类别DTO
     */
    void update(CategoryUpdateDTO dto);

    /**
     * 删除类别
     *
     * @param id 类别ID
     */
    void delete(Long id);

    /**
     * 根据ID获取类别
     *
     * @param id 类别ID
     * @return 类别视图对象
     */
    CategoryVO getById(Long id);

    /**
     * 获取类别列表
     *
     * @return 类别视图对象列表
     */
    List<CategoryVO> list();

    /**
     * 分页查询类别
     *
     * @param dto 查询类别DTO
     * @return 分页类别视图对象
     */
    Page<CategoryVO> page(CategoryQueryDTO dto);

    /**
     * 实体转换为视图对象
     *
     * @param entity 类别实体
     * @return 类别视图对象
     */
    CategoryVO toVO(Category entity);
} 