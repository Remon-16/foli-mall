package com.github.foli_backend.service;

import com.github.foli_backend.dto.response.CategoryVO;
import com.github.foli_backend.entity.FmProductCategory;

import java.util.List;

/**
 * 商品分类服务接口 Product category service interface
 * 提供分类树查询、CRUD操作
 * Provides category tree query and CRUD operations
 */
public interface FmProductCategoryService {

    /**
     * 获取分类树 仅已启用的分类 Get category tree, only enabled categories
     * @return 树形分类列表 Tree category list
     */
    List<CategoryVO> getTree();

    /**
     * 获取所有分类 含禁用和删除的 供管理后台使用
     * Get all categories including disabled and deleted, for admin use
     * @return 所有分类列表 All category list
     */
    List<FmProductCategory> listAll();

    /**
     * 创建分类 Create category
     * @param name 分类名称 Category name
     * @param parentId 父分类ID Parent category ID
     * @param icon 分类图标 Category icon
     * @param sortOrder 排序号 Sort order
     * @return 创建后的分类实体 Created category entity
     */
    FmProductCategory create(String name, Long parentId, String icon, Integer sortOrder);

    /**
     * 更新分类 Update category
     * @param id 分类ID Category ID
     * @param name 分类名称 Category name
     * @param parentId 父分类ID Parent category ID
     * @param icon 分类图标 Category icon
     * @param sortOrder 排序号 Sort order
     * @return 更新后的分类实体 Updated category entity
     */
    FmProductCategory update(Long id, String name, Long parentId, String icon, Integer sortOrder);

    /**
     * 删除分类 逻辑删除 Logical delete
     * 如果存在子分类则抛出异常 Cannot delete if has sub-categories
     * @param id 分类ID Category ID
     */
    void delete(Long id);
}
