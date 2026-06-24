package com.github.foli_backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.foli_backend.constant.BizCodeEnum;
import com.github.foli_backend.dto.response.CategoryVO;
import com.github.foli_backend.entity.FmProductCategory;
import com.github.foli_backend.mapper.FmProductCategoryMapper;
import com.github.foli_backend.service.FmProductCategoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 商品分类服务实现 Product category service implementation
 * 处理分类树查询、增删改操作
 * Handles category tree query and CRUD operations
 */
@Service
public class FmProductCategoryServiceImpl implements FmProductCategoryService {

    private static final Logger log = LoggerFactory.getLogger(FmProductCategoryServiceImpl.class);

    private final FmProductCategoryMapper fmProductCategoryMapper;

    /**
     * 构造器注入 Constructor injection
     */
    public FmProductCategoryServiceImpl(FmProductCategoryMapper fmProductCategoryMapper) {
        this.fmProductCategoryMapper = fmProductCategoryMapper;
    }

    @Override
    public List<CategoryVO> getTree() {
        // 查询所有启用的分类 status=1 Query all enabled categories with status=1
        LambdaQueryWrapper<FmProductCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FmProductCategory::getStatus, 1)
                .orderByAsc(FmProductCategory::getSortOrder);

        List<FmProductCategory> all = fmProductCategoryMapper.selectList(wrapper);

        // 构建分类树 Build category tree
        return CategoryVO.buildTree(all);
    }

    @Override
    public List<FmProductCategory> listAll() {
        LambdaQueryWrapper<FmProductCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(FmProductCategory::getSortOrder);
        return fmProductCategoryMapper.selectList(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FmProductCategory create(String name, Long parentId, String icon, Integer sortOrder) {
        FmProductCategory category = new FmProductCategory();
        category.setName(name);
        category.setParentId(parentId != null ? parentId : 0L);
        category.setIcon(icon);
        category.setSortOrder(sortOrder != null ? sortOrder : 0);
        category.setStatus(1); // 默认启用 Default enabled

        fmProductCategoryMapper.insert(category);
        log.info("Category created: id={}, name={}, parentId={}", category.getId(), name, parentId);
        return category;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FmProductCategory update(Long id, String name, Long parentId, String icon, Integer sortOrder) {
        FmProductCategory category = fmProductCategoryMapper.selectById(id);
        if (category == null) {
            BizCodeEnum.CATEGORY_NOT_FOUND.throwEx();
        }

        if (parentId != null && parentId.equals(id)) {
            BizCodeEnum.CATEGORY_PARENT_SELF.throwEx();
        }

        category.setName(name);
        category.setParentId(parentId != null ? parentId : 0L);
        category.setIcon(icon);
        category.setSortOrder(sortOrder != null ? sortOrder : 0);

        fmProductCategoryMapper.updateById(category);
        log.info("Category updated: id={}, name={}", id, name);
        return category;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        FmProductCategory category = fmProductCategoryMapper.selectById(id);
        if (category == null) {
            BizCodeEnum.CATEGORY_NOT_FOUND.throwEx();
        }

        LambdaQueryWrapper<FmProductCategory> childWrapper = new LambdaQueryWrapper<>();
        childWrapper.eq(FmProductCategory::getParentId, id);
        long childCount = fmProductCategoryMapper.selectCount(childWrapper);

        if (childCount > 0) {
            BizCodeEnum.CATEGORY_HAS_CHILDREN.throwEx();
        }

        fmProductCategoryMapper.deleteById(id);
        log.info("Category deleted: id={}, name={}", id, category.getName());
    }
}
