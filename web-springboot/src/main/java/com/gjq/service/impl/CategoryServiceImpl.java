package com.gjq.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gjq.client.FileClient;
import com.gjq.common.exception.BusinessException;
import com.gjq.dto.category.CategoryAddDTO;
import com.gjq.dto.category.CategoryQueryDTO;
import com.gjq.dto.category.CategoryUpdateDTO;
import com.gjq.entity.Category;
import com.gjq.mapper.CategoryMapper;
import com.gjq.mapper.ItemMapper;
import com.gjq.service.CategoryService;
import com.gjq.vo.category.CategoryVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 类别服务实现类
 */
@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private ItemMapper itemMapper;

    @Autowired
    private FileClient fileClient;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long add(CategoryAddDTO dto) {
        // 检查名称是否已存在
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Category::getName, dto.getName());
        if (categoryMapper.selectCount(queryWrapper) > 0) {
            throw new BusinessException("类别名称已存在");
        }

        // 创建实体并保存
        Category entity = new Category();
        BeanUtils.copyProperties(dto, entity);
        categoryMapper.insert(entity);
        
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(CategoryUpdateDTO dto) {
        // 检查ID是否存在
        Category entity = categoryMapper.selectById(dto.getId());
        if (entity == null) {
            throw new BusinessException("类别不存在");
        }

        // 如果更新名称，检查是否与其他类别重复
        if (StringUtils.hasText(dto.getName()) && !dto.getName().equals(entity.getName())) {
            LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Category::getName, dto.getName());
            queryWrapper.ne(Category::getId, dto.getId());
            if (categoryMapper.selectCount(queryWrapper) > 0) {
                throw new BusinessException("类别名称已存在");
            }
        }

        // 如果更新图标，删除旧的图标文件
        if (dto.getIconObjectKey() != null && !dto.getIconObjectKey().equals(entity.getIconObjectKey()) && 
            StringUtils.hasText(entity.getIconBucket()) && StringUtils.hasText(entity.getIconObjectKey())) {
            try {
                fileClient.delete(entity.getIconBucket(), entity.getIconObjectKey());
            } catch (Exception e) {
                // 文件删除失败，仅记录不影响后续操作
                System.err.println("删除旧的类别图标文件失败: " + e.getMessage());
            }
        }

        // 更新属性
        if (StringUtils.hasText(dto.getName())) {
            entity.setName(dto.getName());
        }
        if (dto.getDescription() != null) {
            entity.setDescription(dto.getDescription());
        }
        if (dto.getIconBucket() != null) {
            entity.setIconBucket(dto.getIconBucket());
        }
        if (dto.getIconObjectKey() != null) {
            entity.setIconObjectKey(dto.getIconObjectKey());
        }

        categoryMapper.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        // 检查ID是否存在
        Category entity = categoryMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("类别不存在");
        }

        // 检查是否有关联的物品
        LambdaQueryWrapper<com.gjq.entity.Item> itemQueryWrapper = new LambdaQueryWrapper<>();
        itemQueryWrapper.eq(com.gjq.entity.Item::getCategoryId, id);
        if (itemMapper.selectCount(itemQueryWrapper) > 0) {
            throw new BusinessException("该类别下有关联的物品，无法删除");
        }

        // 删除图标文件
        if (StringUtils.hasText(entity.getIconBucket()) && StringUtils.hasText(entity.getIconObjectKey())) {
            try {
                fileClient.delete(entity.getIconBucket(), entity.getIconObjectKey());
            } catch (Exception e) {
                // 文件删除失败，仅记录不影响后续操作
                System.err.println("删除类别图标文件失败: " + e.getMessage());
            }
        }

        // 删除类别
        categoryMapper.deleteById(id);
    }

    @Override
    public CategoryVO getById(Long id) {
        Category entity = categoryMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("类别不存在");
        }
        return toVO(entity);
    }

    @Override
    public List<CategoryVO> list() {
        List<Category> list = categoryMapper.selectList(null);
        return list.stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public Page<CategoryVO> page(CategoryQueryDTO dto) {
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        
        // 添加查询条件
        if (StringUtils.hasText(dto.getName())) {
            queryWrapper.like(Category::getName, dto.getName());
        }
        if (StringUtils.hasText(dto.getDescription())) {
            queryWrapper.like(Category::getDescription, dto.getDescription());
        }
        
        // 按创建时间降序排序
        queryWrapper.orderByDesc(Category::getCreateTime);
        
        // 分页查询
        Page<Category> page = new Page<>(dto.getCurrent(), dto.getSize());
        page = categoryMapper.selectPage(page, queryWrapper);
        
        // 转换为VO
        Page<CategoryVO> voPage = new Page<>();
        BeanUtils.copyProperties(page, voPage, "records");
        
        List<CategoryVO> voList = new ArrayList<>();
        for (Category entity : page.getRecords()) {
            voList.add(toVO(entity));
        }
        voPage.setRecords(voList);
        
        return voPage;
    }

    @Override
    public CategoryVO toVO(Category entity) {
        if (entity == null) {
            return null;
        }
        
        CategoryVO vo = new CategoryVO();
        BeanUtils.copyProperties(entity, vo);
        
        // 设置图标URL
        if (StringUtils.hasText(entity.getIconBucket()) && StringUtils.hasText(entity.getIconObjectKey())) {
            vo.setIconUrl(fileClient.getFileUrl(entity.getIconBucket(), entity.getIconObjectKey()));
        }
        
        return vo;
    }
} 