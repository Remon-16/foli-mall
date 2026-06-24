package com.github.foli_backend.controller;

import com.github.foli_backend.common.PageResult;
import com.github.foli_backend.common.Result;
import com.github.foli_backend.dto.request.ProductSearchRequest;
import com.github.foli_backend.dto.response.ProductDetailVO;
import com.github.foli_backend.dto.response.ProductVO;
import com.github.foli_backend.service.FmProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * 商品公开接口控制器 Product public controller
 * 无需登录即可访问的商品搜索和详情接口
 * Public product search and detail endpoints (no login required)
 */
@RestController
@RequestMapping("/api/products")
@Tag(name = "商品管理 Products", description = "商品搜索和详情 Product search and detail")
public class ProductController {

    private final FmProductService productService;

    /** 构造函数注入 Constructor injection */
    public ProductController(FmProductService productService) {
        this.productService = productService;
    }

    /**
     * 公开商品搜索（分页、筛选、排序）
     * Public product search (paginated, filtered, sorted)
     */
    @GetMapping
    @Operation(summary = "搜索商品 Search products", description = "公开接口，支持关键词、分类、价格区间、排序  Public search with keyword, category, price range, sorting")
    public Result<PageResult<ProductVO>> searchProducts(
            @Parameter(description = "搜索关键词 Search keyword")
            @RequestParam(required = false) String keyword,
            @Parameter(description = "分类ID Category ID")
            @RequestParam(required = false) Long categoryId,
            @Parameter(description = "最低价格 Minimum price")
            @RequestParam(required = false) BigDecimal minPrice,
            @Parameter(description = "最高价格 Maximum price")
            @RequestParam(required = false) BigDecimal maxPrice,
            @Parameter(description = "排序方式: default/newest/price_asc/price_desc/sales Sort by")
            @RequestParam(required = false) String sortBy,
            @Parameter(description = "页码 Page number", example = "1")
            @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小 Page size", example = "20")
            @RequestParam(defaultValue = "20") Integer pageSize) {

        // 组装请求对象 assemble request object
        ProductSearchRequest req = new ProductSearchRequest();
        req.setKeyword(keyword);
        req.setCategoryId(categoryId);
        req.setMinPrice(minPrice);
        req.setMaxPrice(maxPrice);
        req.setSortBy(sortBy);
        req.setPage(page);
        req.setPageSize(pageSize);

        PageResult<ProductVO> result = productService.searchProducts(req);
        return Result.success(result);
    }

    /**
     * 获取商品详情（含店铺名、分类名、图片列表）
     * Get product detail (with store name, category name, image list)
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取商品详情 Get product detail", description = "公开接口，返回商品完整信息包括图片列表 Public detail with full info including image list")
    public Result<ProductDetailVO> getProductDetail(
            @Parameter(description = "商品ID Product ID") @PathVariable Long id) {
        ProductDetailVO detail = productService.getProductDetail(id);
        return Result.success(detail);
    }
}
