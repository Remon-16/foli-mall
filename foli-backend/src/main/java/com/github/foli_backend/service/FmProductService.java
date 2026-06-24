package com.github.foli_backend.service;

import com.github.foli_backend.common.PageResult;
import com.github.foli_backend.dto.request.ProductPublishRequest;
import com.github.foli_backend.dto.request.ProductReviewRequest;
import com.github.foli_backend.dto.request.ProductSearchRequest;
import com.github.foli_backend.dto.response.ProductDetailVO;
import com.github.foli_backend.dto.response.ProductVO;
import com.github.foli_backend.entity.FmProduct;

/**
 * 商品服务接口 Product service interface
 */
public interface FmProductService {

    /**
     * 公开商品搜索（仅返回已审核通过的商品）
     * Public product search (only returns APPROVED products)
     * 支持关键词模糊搜索、分类筛选、价格区间、排序
     * Supports keyword fuzzy search, category filter, price range, sorting
     *
     * @param req 搜索请求 search request
     * @return 分页商品列表 paginated product list
     */
    PageResult<ProductVO> searchProducts(ProductSearchRequest req);

    /**
     * 获取商品详情（含店铺名、分类名、图片列表）
     * Get product detail (with store name, category name, image list)
     *
     * @param productId 商品ID product ID
     * @return 商品详情 product detail
     */
    ProductDetailVO getProductDetail(Long productId);

    /**
     * 卖家查询自己的商品列表（可按状态筛选）
     * Seller lists own products (filterable by status)
     *
     * @param storeId  店铺ID store ID
     * @param page     页码 page number
     * @param pageSize 每页大小 page size
     * @param status   商品状态（可选） product status (optional)
     * @return 分页商品列表 paginated product list
     */
    PageResult<ProductVO> listSellerProducts(Long storeId, int page, int pageSize, Integer status);

    /**
     * 卖家发布新商品（状态为待审核 PENDING_REVIEW）
     * Seller publishes a new product (status = PENDING_REVIEW)
     *
     * @param storeId 店铺ID store ID
     * @param req     发布请求 publish request
     * @return 创建的商品 created product
     */
    FmProduct publishProduct(Long storeId, ProductPublishRequest req);

    /**
     * 卖家更新商品信息（验证所属权，重置状态为待审核）
     * Seller updates product info (verifies ownership, resets status to PENDING_REVIEW)
     *
     * @param productId 商品ID product ID
     * @param storeId   店铺ID store ID
     * @param req       更新请求 update request
     * @return 更新后的商品 updated product
     */
    FmProduct updateProduct(Long productId, Long storeId, ProductPublishRequest req);

    /**
     * 卖家删除商品（逻辑删除，验证所属权）
     * Seller deletes product (logical delete, verifies ownership)
     *
     * @param productId 商品ID product ID
     * @param storeId   店铺ID store ID
     */
    void deleteProduct(Long productId, Long storeId);

    /**
     * 卖家下架商品（状态设为 OFF_SHELF）
     * Seller takes product off shelf (status = OFF_SHELF)
     *
     * @param productId 商品ID product ID
     * @param storeId   店铺ID store ID
     */
    void offShelf(Long productId, Long storeId);

    /**
     * 卖家重新上架商品（状态设为 PENDING_REVIEW 以待重新审核）
     * Seller puts product back on shelf (status = PENDING_REVIEW for re-review)
     *
     * @param productId 商品ID product ID
     * @param storeId   店铺ID store ID
     */
    void onShelf(Long productId, Long storeId);

    /**
     * 管理员查询待审核商品列表
     * Admin lists pending review products
     *
     * @param page     页码 page number
     * @param pageSize 每页大小 page size
     * @return 分页商品列表 paginated product list
     */
    PageResult<ProductVO> listPendingProducts(int page, int pageSize);

    /**
     * 管理员查询所有商品列表（可按状态筛选）
     * Admin lists all products (filterable by status)
     *
     * @param page     页码 page number
     * @param pageSize 每页大小 page size
     * @param status   商品状态（可选） product status (optional)
     * @return 分页商品列表 paginated product list
     */
    PageResult<ProductVO> listAllProducts(int page, int pageSize, Integer status);

    /**
     * 管理员审核商品
     * Admin reviews product
     *
     * @param productId 商品ID product ID
     * @param req       审核请求 review request
     */
    void reviewProduct(Long productId, ProductReviewRequest req);
}
