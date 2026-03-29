package com.gjq.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gjq.dto.item.ItemAddDTO;
import com.gjq.dto.item.ItemQueryDTO;
import com.gjq.dto.item.ItemUpdateDTO;
import com.gjq.entity.Item;
import com.gjq.vo.item.ItemVO;

import java.util.List;

/**
 * 物品服务接口
 */
public interface ItemService {

    /**
     * 添加物品
     *
     * @param dto 添加物品DTO
     * @return 物品ID
     */
    Long add(ItemAddDTO dto);

    /**
     * 更新物品
     *
     * @param dto 更新物品DTO
     */
    void update(ItemUpdateDTO dto);

    /**
     * 删除物品
     *
     * @param id 物品ID
     */
    void delete(Long id);

    /**
     * 根据ID获取物品
     *
     * @param id 物品ID
     * @return 物品视图对象
     */
    ItemVO getById(Long id);

    /**
     * 分页查询物品
     *
     * @param dto 查询物品DTO
     * @return 分页物品视图对象
     */
    Page<ItemVO> page(ItemQueryDTO dto);

    /**
     * 根据类别ID获取物品列表
     *
     * @param categoryId 类别ID
     * @return 物品视图对象列表
     */
    List<ItemVO> listByCategoryId(Long categoryId);

    /**
     * 根据标签获取物品列表
     *
     * @param tag 标签
     * @return 物品视图对象列表
     */
    List<ItemVO> listByTag(String tag);

    /**
     * 实体转换为视图对象
     *
     * @param entity 物品实体
     * @return 物品视图对象
     */
    ItemVO toVO(Item entity);
} 