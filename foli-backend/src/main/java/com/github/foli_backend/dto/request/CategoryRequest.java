package com.github.foli_backend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * 分类创建/更新请求 Category create/update request DTO
 */
@Schema(description = "分类请求 Category request")
public class CategoryRequest {

    @Schema(description = "分类名称 Category name", example = "电子产品")
    @NotBlank(message = "分类名称不能为空 Category name cannot be blank")
    private String name;

    @Schema(description = "父分类ID Parent category ID, 0表示顶级分类 0 means top-level", example = "0")
    private Long parentId;

    @Schema(description = "分类图标 Category icon", example = "icon-electronics")
    private String icon;

    @Schema(description = "排序号 Sort order", example = "1")
    private Integer sortOrder;

    // ========== Getters and Setters ==========

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
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
}
