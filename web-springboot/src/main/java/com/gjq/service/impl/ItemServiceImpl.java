package com.gjq.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gjq.client.FileClient;
import com.gjq.common.exception.BusinessException;
import com.gjq.dto.item.ItemAddDTO;
import com.gjq.dto.item.ItemQueryDTO;
import com.gjq.dto.item.ItemUpdateDTO;
import com.gjq.entity.Category;
import com.gjq.entity.Item;
import com.gjq.entity.User;
import com.gjq.mapper.CategoryMapper;
import com.gjq.mapper.ItemMapper;
import com.gjq.mapper.UserMapper;
import com.gjq.service.CategoryService;
import com.gjq.service.ItemService;
import com.gjq.vo.item.ItemVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 物品服务实现类
 */
@Service
public class ItemServiceImpl implements ItemService {

    @Autowired
    private ItemMapper itemMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private FileClient fileClient;

    @Autowired
    private UserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long add(ItemAddDTO dto) {
        // 检查类别是否存在
        Category category = categoryMapper.selectById(dto.getCategoryId());
        if (category == null) {
            throw new BusinessException("类别不存在");
        }

        // 创建实体并保存
        Item entity = new Item();
        BeanUtils.copyProperties(dto, entity);
        itemMapper.insert(entity);
        
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(ItemUpdateDTO dto) {
        // 检查ID是否存在
        Item entity = itemMapper.selectById(dto.getId());
        if (entity == null) {
            throw new BusinessException("物品不存在");
        }

        // 如果更新类别ID，检查类别是否存在
        if (dto.getCategoryId() != null && !dto.getCategoryId().equals(entity.getCategoryId())) {
            Category category = categoryMapper.selectById(dto.getCategoryId());
            if (category == null) {
                throw new BusinessException("类别不存在");
            }
        }

        // 如果更新封面，删除旧的封面文件
        if (dto.getCoverObjectKey() != null && !dto.getCoverObjectKey().equals(entity.getCoverObjectKey()) && 
            StringUtils.hasText(entity.getCoverBucket()) && StringUtils.hasText(entity.getCoverObjectKey())) {
            try {
                fileClient.delete(entity.getCoverBucket(), entity.getCoverObjectKey());
            } catch (Exception e) {
                // 文件删除失败，仅记录不影响后续操作
                System.err.println("删除旧的物品封面文件失败: " + e.getMessage());
            }
        }

        // 如果更新文件，删除旧的文件
        if (dto.getFileObjectKey() != null && !dto.getFileObjectKey().equals(entity.getFileObjectKey()) && 
            StringUtils.hasText(entity.getFileBucket()) && StringUtils.hasText(entity.getFileObjectKey())) {
            try {
                fileClient.delete(entity.getFileBucket(), entity.getFileObjectKey());
            } catch (Exception e) {
                // 文件删除失败，仅记录不影响后续操作
                System.err.println("删除旧的物品文件失败: " + e.getMessage());
            }
        }

        // 更新属性
        if (StringUtils.hasText(dto.getTitle())) {
            entity.setTitle(dto.getTitle());
        }
        if (dto.getDescription() != null) {
            entity.setDescription(dto.getDescription());
        }
        if (dto.getCategoryId() != null) {
            entity.setCategoryId(dto.getCategoryId());
        }
        if (dto.getCoverBucket() != null) {
            entity.setCoverBucket(dto.getCoverBucket());
        }
        if (dto.getCoverObjectKey() != null) {
            entity.setCoverObjectKey(dto.getCoverObjectKey());
        }
        if (dto.getFileBucket() != null) {
            entity.setFileBucket(dto.getFileBucket());
        }
        if (dto.getFileObjectKey() != null) {
            entity.setFileObjectKey(dto.getFileObjectKey());
        }
        if (dto.getTags() != null) {
            entity.setTags(dto.getTags());
        }
        if (dto.getExtraData() != null) {
            entity.setExtraData(dto.getExtraData());
        }

        itemMapper.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        // 检查ID是否存在
        Item entity = itemMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("物品不存在");
        }

        // 删除封面文件
        if (StringUtils.hasText(entity.getCoverBucket()) && StringUtils.hasText(entity.getCoverObjectKey())) {
            try {
                fileClient.delete(entity.getCoverBucket(), entity.getCoverObjectKey());
            } catch (Exception e) {
                // 文件删除失败，仅记录不影响后续操作
                System.err.println("删除物品封面文件失败: " + e.getMessage());
            }
        }

        // 删除相关文件
        if (StringUtils.hasText(entity.getFileBucket()) && StringUtils.hasText(entity.getFileObjectKey())) {
            try {
                fileClient.delete(entity.getFileBucket(), entity.getFileObjectKey());
            } catch (Exception e) {
                // 文件删除失败，仅记录不影响后续操作
                System.err.println("删除物品相关文件失败: " + e.getMessage());
            }
        }

        // 删除物品
        itemMapper.deleteById(id);
    }

    @Override
    public ItemVO getById(Long id) {
        Item entity = itemMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("物品不存在");
        }
        return toVO(entity);
    }

    @Override
    public Page<ItemVO> page(ItemQueryDTO dto) {
        LambdaQueryWrapper<Item> queryWrapper = new LambdaQueryWrapper<>();
        
        // 添加查询条件
        if (StringUtils.hasText(dto.getTitle())) {
            queryWrapper.like(Item::getTitle, dto.getTitle());
        }
        if (StringUtils.hasText(dto.getDescription())) {
            queryWrapper.like(Item::getDescription, dto.getDescription());
        }
        if (dto.getCategoryId() != null) {
            queryWrapper.eq(Item::getCategoryId, dto.getCategoryId());
        }
        if (StringUtils.hasText(dto.getTag())) {
            // 处理标签搜索 - 支持多个标签
            String tagQuery = dto.getTag().trim();
            
            // 如果包含逗号，说明是多个标签
            if (tagQuery.contains(",")) {
                // 分割多个标签
                String[] tags = tagQuery.split(",");
                for (String tag : tags) {
                    if (StringUtils.hasText(tag)) {
                        // 确保每个标签前后都有逗号或在开头结尾，防止部分匹配
                        // 例如: 标签为"科学,技术"，搜索"科"应该能匹配，但搜索"学技"不应该匹配
                        queryWrapper.and(q -> q.like(Item::getTags, "," + tag.trim() + ",")
                                .or()
                                .like(Item::getTags, tag.trim() + ",")
                                .or()
                                .like(Item::getTags, "," + tag.trim())
                                .or()
                                .eq(Item::getTags, tag.trim()));
                    }
                }
            } else {
                // 单个标签搜索
                // 确保标签前后都有逗号或在开头结尾，防止部分匹配
                queryWrapper.and(q -> q.like(Item::getTags, "," + tagQuery + ",")
                        .or()
                        .like(Item::getTags, tagQuery + ",")
                        .or()
                        .like(Item::getTags, "," + tagQuery)
                        .or()
                        .eq(Item::getTags, tagQuery));
            }
        }
        
        // 查询创建者真实姓名
        if (StringUtils.hasText(dto.getUserRealName())) {
            // 查询符合真实姓名条件的用户ID列表
            LambdaQueryWrapper<User> userQuery = new LambdaQueryWrapper<>();
            userQuery.like(User::getRealName, dto.getUserRealName());
            List<User> users = userMapper.selectList(userQuery);
            List<Long> userIds = users.stream().map(User::getId).collect(Collectors.toList());
            
            // 如果没有找到匹配的用户，添加一个不可能的条件确保查询结果为空
            if (userIds.isEmpty()) {
                queryWrapper.eq(Item::getUserId, -1L);
            } else {
                queryWrapper.in(Item::getUserId, userIds);
            }
        }
        
        // 按创建时间降序排序
        queryWrapper.orderByDesc(Item::getCreateTime);
        
        // 分页查询
        Page<Item> page = new Page<>(dto.getCurrent(), dto.getSize());
        page = itemMapper.selectPage(page, queryWrapper);
        
        // 转换为VO
        Page<ItemVO> voPage = new Page<>();
        BeanUtils.copyProperties(page, voPage, "records");
        
        List<ItemVO> voList = new ArrayList<>();
        for (Item entity : page.getRecords()) {
            voList.add(toVO(entity));
        }
        voPage.setRecords(voList);
        
        return voPage;
    }

    @Override
    public List<ItemVO> listByCategoryId(Long categoryId) {
        LambdaQueryWrapper<Item> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Item::getCategoryId, categoryId);
        queryWrapper.orderByDesc(Item::getCreateTime);
        
        List<Item> list = itemMapper.selectList(queryWrapper);
        return list.stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public List<ItemVO> listByTag(String tag) {
        if (!StringUtils.hasText(tag)) {
            return new ArrayList<>();
        }
        
        LambdaQueryWrapper<Item> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(Item::getTags, tag);
        queryWrapper.orderByDesc(Item::getCreateTime);
        
        List<Item> list = itemMapper.selectList(queryWrapper);
        return list.stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public ItemVO toVO(Item entity) {
        if (entity == null) {
            return null;
        }
        
        ItemVO vo = new ItemVO();
        BeanUtils.copyProperties(entity, vo);
        
        // 设置封面URL
        if (StringUtils.hasText(entity.getCoverBucket()) && StringUtils.hasText(entity.getCoverObjectKey())) {
            vo.setCoverUrl(fileClient.getFileUrl(entity.getCoverBucket(), entity.getCoverObjectKey()));
        }
        
        // 设置文件URL
        if (StringUtils.hasText(entity.getFileBucket()) && StringUtils.hasText(entity.getFileObjectKey())) {
            vo.setFileUrl(fileClient.getFileUrl(entity.getFileBucket(), entity.getFileObjectKey()));
        }
        
        // 设置标签列表
        if (StringUtils.hasText(entity.getTags())) {
            vo.setTagList(entity.getTags().split(","));
        }
        
        // 设置类别信息
        Category category = categoryMapper.selectById(entity.getCategoryId());
        if (category != null) {
            vo.setCategory(categoryService.toVO(category));
        }
        
        // 设置发布者真实姓名
        if (entity.getUserId() != null) {
            User user = userMapper.selectById(entity.getUserId());
            if (user != null) {
                vo.setUserRealName(user.getRealName());
            }
        }
        
        return vo;
    }
} 