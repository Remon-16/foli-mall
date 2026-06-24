package com.github.foli_backend.controller;

import com.github.foli_backend.annotation.RequireLogin;
import com.github.foli_backend.annotation.RequireRole;
import com.github.foli_backend.common.Result;
import com.github.foli_backend.constant.RoleConstants;
import com.github.foli_backend.dto.request.CategoryRequest;
import com.github.foli_backend.dto.response.CategoryVO;
import com.github.foli_backend.entity.FmProductCategory;
import com.github.foli_backend.service.FmProductCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 分类管理控制器 Category management controller
 * 提供分类树查询、增删改接口
 * Provides category tree query and CRUD endpoints
 */
@RestController
@RequestMapping("/api/categories")
@Tag(name = "分类管理 Categories")
public class CategoryController {

    private final FmProductCategoryService fmProductCategoryService;

    /**
     * 构造器注入 Constructor injection
     */
    public CategoryController(FmProductCategoryService fmProductCategoryService) {
        this.fmProductCategoryService = fmProductCategoryService;
    }

    /**
     * 获取分类树 公开接口 Get category tree, public endpoint
     */
    @GetMapping
    @Operation(summary = "获取分类树 Get category tree", description = "获取所有已启用分类的树形结构 Get tree structure of all enabled categories")
    public Result<List<CategoryVO>> getTree() {
        List<CategoryVO> tree = fmProductCategoryService.getTree();
        return Result.success(tree);
    }

    /**
     * 创建分类 管理员接口 Create category, admin only
     */
    @PostMapping
    @RequireLogin
    @RequireRole(value = {RoleConstants.ADMIN})
    @Operation(summary = "创建分类 Create category", description = "管理员创建新的商品分类 Admin creates a new product category")
    public Result<CategoryVO> create(@Valid @RequestBody CategoryRequest request) {
        FmProductCategory category = fmProductCategoryService.create(
                request.getName(),
                request.getParentId(),
                request.getIcon(),
                request.getSortOrder()
        );
        return Result.success(CategoryVO.fromEntity(category));
    }

    /**
     * 更新分类 管理员接口 Update category, admin only
     */
    @PutMapping("/{id}")
    @RequireLogin
    @RequireRole(value = {RoleConstants.ADMIN})
    @Operation(summary = "更新分类 Update category", description = "管理员更新指定的商品分类 Admin updates a specific product category")
    public Result<CategoryVO> update(
            @Parameter(description = "分类ID Category ID") @PathVariable Long id,
            @Valid @RequestBody CategoryRequest request) {
        FmProductCategory category = fmProductCategoryService.update(
                id,
                request.getName(),
                request.getParentId(),
                request.getIcon(),
                request.getSortOrder()
        );
        return Result.success(CategoryVO.fromEntity(category));
    }

    /**
     * 删除分类 管理员接口 Delete category, admin only
     */
    @DeleteMapping("/{id}")
    @RequireLogin
    @RequireRole(value = {RoleConstants.ADMIN})
    @Operation(summary = "删除分类 Delete category", description = "管理员逻辑删除指定的商品分类 存在子分类则无法删除 Admin logically deletes a category; cannot delete if sub-categories exist")
    public Result<Void> delete(
            @Parameter(description = "分类ID Category ID") @PathVariable Long id) {
        fmProductCategoryService.delete(id);
        return Result.success();
    }
}
