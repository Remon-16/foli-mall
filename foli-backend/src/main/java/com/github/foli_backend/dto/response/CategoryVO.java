package com.github.foli_backend.dto.response;

import com.github.foli_backend.entity.FmProductCategory;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 分类视图对象 Category view object
 * 支持树形结构展示 Supports tree structure display
 */
@Schema(description = "分类视图 Category view")
public class CategoryVO {

    @Schema(description = "分类ID Category ID")
    private Long id;

    @Schema(description = "父分类ID Parent category ID")
    private Long parentId;

    @Schema(description = "分类名称 Category name")
    private String name;

    @Schema(description = "分类图标 Category icon")
    private String icon;

    @Schema(description = "排序号 Sort order")
    private Integer sortOrder;

    @Schema(description = "子分类列表 Child categories")
    private List<CategoryVO> children;

    /**
     * 从实体转换 不含子分类 Convert from entity without children
     * @param cat 分类实体 Category entity
     * @return 视图对象 View object
     */
    public static CategoryVO fromEntity(FmProductCategory cat) {
        CategoryVO vo = new CategoryVO();
        vo.setId(cat.getId());
        vo.setParentId(cat.getParentId());
        vo.setName(cat.getName());
        vo.setIcon(cat.getIcon());
        vo.setSortOrder(cat.getSortOrder());
        vo.setChildren(new ArrayList<>());
        return vo;
    }

    /**
     * 构建分类树 Build category tree
     * 将所有平面分类列表构建为树形结构 Build tree structure from flat category list
     * @param all 所有分类列表 All category list
     * @return 树形分类列表 Tree category list (top-level items)
     */
    public static List<CategoryVO> buildTree(List<FmProductCategory> all) {
        // 将所有实体转换为VO Convert all entities to VO
        List<CategoryVO> allVos = all.stream()
                .map(CategoryVO::fromEntity)
                .collect(Collectors.toList());

        // 找出顶级分类 parentId为0 Find top-level categories where parentId is 0
        List<CategoryVO> roots = new ArrayList<>();
        for (CategoryVO vo : allVos) {
            if (vo.getParentId() == null || vo.getParentId() == 0) {
                roots.add(vo);
            }
        }

        // 递归挂载子分类 Recursively attach children
        for (CategoryVO root : roots) {
            attachChildren(root, allVos);
        }

        return roots;
    }

    /**
     * 递归挂载子分类到父分类 Recursively attach children to parent
     * @param parent 父分类 Parent category
     * @param all 所有分类列表 All category list
     */
    private static void attachChildren(CategoryVO parent, List<CategoryVO> all) {
        for (CategoryVO vo : all) {
            if (parent.getId().equals(vo.getParentId())) {
                parent.getChildren().add(vo);
                attachChildren(vo, all);
            }
        }
    }

    // ========== Getters and Setters ==========

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public List<CategoryVO> getChildren() {
        return children;
    }

    public void setChildren(List<CategoryVO> children) {
        this.children = children;
    }
}
