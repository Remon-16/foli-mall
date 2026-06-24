package com.github.foli_backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.foli_backend.common.PageResult;
import com.github.foli_backend.constant.BizCodeEnum;
import com.github.foli_backend.constant.ProductStatusEnum;
import com.github.foli_backend.dto.request.ProductPublishRequest;
import com.github.foli_backend.dto.request.ProductReviewRequest;
import com.github.foli_backend.dto.request.ProductSearchRequest;
import com.github.foli_backend.dto.response.ProductDetailVO;
import com.github.foli_backend.dto.response.ProductVO;
import com.github.foli_backend.entity.FmProduct;
import com.github.foli_backend.entity.FmProductCategory;
import com.github.foli_backend.entity.FmProductImage;
import com.github.foli_backend.entity.FmStore;
import com.github.foli_backend.mapper.FmProductCategoryMapper;
import com.github.foli_backend.mapper.FmProductImageMapper;
import com.github.foli_backend.mapper.FmProductMapper;
import com.github.foli_backend.mapper.FmStoreMapper;
import com.github.foli_backend.service.FmProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class FmProductServiceImpl implements FmProductService {

    private static final Logger log = LoggerFactory.getLogger(FmProductServiceImpl.class);

    private final FmProductMapper productMapper;
    private final FmProductImageMapper productImageMapper;
    private final FmStoreMapper storeMapper;
    private final FmProductCategoryMapper categoryMapper;

    public FmProductServiceImpl(FmProductMapper productMapper,
                                FmProductImageMapper productImageMapper,
                                FmStoreMapper storeMapper,
                                FmProductCategoryMapper categoryMapper) {
        this.productMapper = productMapper;
        this.productImageMapper = productImageMapper;
        this.storeMapper = storeMapper;
        this.categoryMapper = categoryMapper;
    }

    // ==================== 公开接口 Public APIs ====================

    @Override
    public PageResult<ProductVO> searchProducts(ProductSearchRequest req) {
        LambdaQueryWrapper<FmProduct> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FmProduct::getStatus, ProductStatusEnum.APPROVED.getCode());

        if (StringUtils.hasLength(req.getKeyword())) {
            wrapper.like(FmProduct::getName, req.getKeyword());
        }

        if (req.getCategoryId() != null) {
            List<Long> categoryIds = getCategoryAndChildrenIds(req.getCategoryId());
            if (!categoryIds.isEmpty()) {
                wrapper.in(FmProduct::getCategoryId, categoryIds);
            }
        }

        if (req.getMinPrice() != null) {
            wrapper.ge(FmProduct::getPrice, req.getMinPrice());
        }
        if (req.getMaxPrice() != null) {
            wrapper.le(FmProduct::getPrice, req.getMaxPrice());
        }

        applySorting(wrapper, req.getSortBy());

        Page<FmProduct> mpPage = new Page<>(req.getPage(), req.getPageSize());
        mpPage = productMapper.selectPage(mpPage, wrapper);

        List<ProductVO> voList = mpPage.getRecords().stream()
                .map(ProductVO::fromEntity)
                .collect(Collectors.toList());
        fillStoreNames(voList);

        return new PageResult<>(mpPage.getTotal(), req.getPage(), req.getPageSize(), voList);
    }

    @Override
    public ProductDetailVO getProductDetail(Long productId) {
        FmProduct product = productMapper.selectById(productId);
        if (product == null) {
            BizCodeEnum.PRODUCT_NOT_FOUND.throwEx();
        }

        ProductDetailVO vo = new ProductDetailVO();
        vo.setId(product.getId());
        vo.setStoreId(product.getStoreId());
        vo.setCategoryId(product.getCategoryId());
        vo.setName(product.getName());
        vo.setDescription(product.getDescription());
        vo.setMainImage(product.getMainImage());
        vo.setPrice(product.getPrice());
        vo.setStock(product.getStock());
        vo.setStatus(product.getStatus());
        vo.setSalesCount(product.getSalesCount());
        vo.setCreateTime(product.getCreateTime());

        if (product.getStoreId() != null) {
            FmStore store = storeMapper.selectById(product.getStoreId());
            if (store != null) {
                vo.setStoreName(store.getStoreName());
            }
        }

        if (product.getCategoryId() != null) {
            FmProductCategory category = categoryMapper.selectById(product.getCategoryId());
            if (category != null) {
                vo.setCategoryName(category.getName());
            }
        }

        List<FmProductImage> images = productImageMapper.selectList(
                new LambdaQueryWrapper<FmProductImage>()
                        .eq(FmProductImage::getProductId, productId)
                        .orderByAsc(FmProductImage::getSortOrder)
        );
        List<String> imageUrls = images.stream()
                .map(FmProductImage::getImageUrl)
                .collect(Collectors.toList());
        vo.setImages(imageUrls);

        return vo;
    }

    // ==================== 卖家接口 Seller APIs ====================

    @Override
    public PageResult<ProductVO> listSellerProducts(Long storeId, int page, int pageSize, Integer status) {
        LambdaQueryWrapper<FmProduct> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FmProduct::getStoreId, storeId);

        if (status != null) {
            wrapper.eq(FmProduct::getStatus, status);
        }

        wrapper.orderByDesc(FmProduct::getCreateTime);

        Page<FmProduct> mpPage = new Page<>(page, pageSize);
        mpPage = productMapper.selectPage(mpPage, wrapper);

        List<ProductVO> voList = mpPage.getRecords().stream()
                .map(ProductVO::fromEntity)
                .collect(Collectors.toList());

        return new PageResult<>(mpPage.getTotal(), page, pageSize, voList);
    }

    @Override
    @Transactional
    public FmProduct publishProduct(Long storeId, ProductPublishRequest req) {
        FmProduct product = new FmProduct();
        product.setStoreId(storeId);
        product.setCategoryId(req.getCategoryId());
        product.setName(req.getName());
        product.setDescription(req.getDescription());
        product.setMainImage(req.getMainImage());
        product.setPrice(req.getPrice());
        product.setStock(req.getStock() != null ? req.getStock() : 0);
        product.setSalesCount(0);
        product.setStatus(ProductStatusEnum.PENDING_REVIEW.getCode());

        productMapper.insert(product);

        saveProductImages(product.getId(), req.getImages());

        log.info("商品发布成功 Product published: id={}, name={}, storeId={}", product.getId(), product.getName(), storeId);
        return product;
    }

    @Override
    @Transactional
    public FmProduct updateProduct(Long productId, Long storeId, ProductPublishRequest req) {
        FmProduct product = productMapper.selectById(productId);
        if (product == null) {
            BizCodeEnum.PRODUCT_NOT_FOUND.throwEx();
        }

        if (!product.getStoreId().equals(storeId)) {
            BizCodeEnum.FORBIDDEN.throwEx();
        }

        product.setCategoryId(req.getCategoryId());
        product.setName(req.getName());
        product.setDescription(req.getDescription());
        product.setMainImage(req.getMainImage());
        product.setPrice(req.getPrice());
        product.setStock(req.getStock() != null ? req.getStock() : 0);
        product.setStatus(ProductStatusEnum.PENDING_REVIEW.getCode());
        product.setReviewComment(null);

        productMapper.updateById(product);

        deleteProductImages(productId);
        saveProductImages(productId, req.getImages());

        log.info("商品更新成功 Product updated: id={}, name={}", productId, product.getName());
        return product;
    }

    @Override
    @Transactional
    public void deleteProduct(Long productId, Long storeId) {
        FmProduct product = productMapper.selectById(productId);
        if (product == null) {
            BizCodeEnum.PRODUCT_NOT_FOUND.throwEx();
        }

        if (!product.getStoreId().equals(storeId)) {
            BizCodeEnum.FORBIDDEN.throwEx();
        }

        productMapper.deleteById(productId);

        log.info("商品删除成功 Product deleted: id={}", productId);
    }

    @Override
    public void offShelf(Long productId, Long storeId) {
        FmProduct product = productMapper.selectById(productId);
        if (product == null) {
            BizCodeEnum.PRODUCT_NOT_FOUND.throwEx();
        }

        if (!product.getStoreId().equals(storeId)) {
            BizCodeEnum.FORBIDDEN.throwEx();
        }

        product.setStatus(ProductStatusEnum.OFF_SHELF.getCode());
        productMapper.updateById(product);

        log.info("商品下架成功 Product off-shelf: id={}", productId);
    }

    @Override
    public void onShelf(Long productId, Long storeId) {
        FmProduct product = productMapper.selectById(productId);
        if (product == null) {
            BizCodeEnum.PRODUCT_NOT_FOUND.throwEx();
        }

        if (!product.getStoreId().equals(storeId)) {
            BizCodeEnum.FORBIDDEN.throwEx();
        }

        product.setStatus(ProductStatusEnum.PENDING_REVIEW.getCode());
        product.setReviewComment(null);
        productMapper.updateById(product);

        log.info("商品上架成功 Product on-shelf: id={}", productId);
    }

    // ==================== 管理员接口 Admin APIs ====================

    @Override
    public PageResult<ProductVO> listPendingProducts(int page, int pageSize) {
        LambdaQueryWrapper<FmProduct> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FmProduct::getStatus, ProductStatusEnum.PENDING_REVIEW.getCode());
        wrapper.orderByAsc(FmProduct::getCreateTime);

        Page<FmProduct> mpPage = new Page<>(page, pageSize);
        mpPage = productMapper.selectPage(mpPage, wrapper);

        List<ProductVO> voList = mpPage.getRecords().stream()
                .map(ProductVO::fromEntity)
                .collect(Collectors.toList());
        fillStoreNames(voList);

        return new PageResult<>(mpPage.getTotal(), page, pageSize, voList);
    }

    @Override
    public PageResult<ProductVO> listAllProducts(int page, int pageSize, Integer status) {
        LambdaQueryWrapper<FmProduct> wrapper = new LambdaQueryWrapper<>();

        if (status != null) {
            wrapper.eq(FmProduct::getStatus, status);
        }

        wrapper.orderByDesc(FmProduct::getCreateTime);

        Page<FmProduct> mpPage = new Page<>(page, pageSize);
        mpPage = productMapper.selectPage(mpPage, wrapper);

        List<ProductVO> voList = mpPage.getRecords().stream()
                .map(ProductVO::fromEntity)
                .collect(Collectors.toList());
        fillStoreNames(voList);

        return new PageResult<>(mpPage.getTotal(), page, pageSize, voList);
    }

    @Override
    public void reviewProduct(Long productId, ProductReviewRequest req) {
        FmProduct product = productMapper.selectById(productId);
        if (product == null) {
            BizCodeEnum.PRODUCT_NOT_FOUND.throwEx();
        }

        // 状态机校验：仅待审核状态可审核
        if (product.getStatus() != ProductStatusEnum.PENDING_REVIEW.getCode()) {
            BizCodeEnum.PRODUCT_ALREADY_REVIEWED.throwEx();
        }

        int status = req.getStatus();
        if (status != ProductStatusEnum.APPROVED.getCode() && status != ProductStatusEnum.REJECTED.getCode()) {
            BizCodeEnum.INVALID_REVIEW_STATUS.throwEx();
        }

        product.setStatus(status);
        product.setReviewComment(req.getReviewComment());
        productMapper.updateById(product);

        log.info("商品审核完成 Product reviewed: id={}, status={}", productId, status);
    }

    // ==================== 私有辅助方法 Private helper methods ====================

    private void applySorting(LambdaQueryWrapper<FmProduct> wrapper, String sortBy) {
        if (sortBy == null || "default".equals(sortBy)) {
            wrapper.orderByDesc(FmProduct::getCreateTime);
        } else {
            switch (sortBy) {
                case "newest":
                    wrapper.orderByDesc(FmProduct::getCreateTime);
                    break;
                case "price_asc":
                    wrapper.orderByAsc(FmProduct::getPrice);
                    break;
                case "price_desc":
                    wrapper.orderByDesc(FmProduct::getPrice);
                    break;
                case "sales":
                    wrapper.orderByDesc(FmProduct::getSalesCount);
                    break;
                default:
                    wrapper.orderByDesc(FmProduct::getCreateTime);
            }
        }
    }

    private List<Long> getCategoryAndChildrenIds(Long categoryId) {
        Set<Long> result = new HashSet<>();
        result.add(categoryId);

        List<FmProductCategory> allCategories = categoryMapper.selectList(null);
        Map<Long, List<FmProductCategory>> parentMap = allCategories.stream()
                .collect(Collectors.groupingBy(FmProductCategory::getParentId));

        collectChildren(categoryId, parentMap, result);

        return new ArrayList<>(result);
    }

    private void collectChildren(Long parentId, Map<Long, List<FmProductCategory>> parentMap, Set<Long> result) {
        List<FmProductCategory> children = parentMap.get(parentId);
        if (children != null && !children.isEmpty()) {
            for (FmProductCategory child : children) {
                if (result.add(child.getId())) {
                    collectChildren(child.getId(), parentMap, result);
                }
            }
        }
    }

    private void saveProductImages(Long productId, List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            return;
        }
        for (int i = 0; i < imageUrls.size(); i++) {
            FmProductImage image = new FmProductImage();
            image.setProductId(productId);
            image.setImageUrl(imageUrls.get(i));
            image.setSortOrder(i + 1);
            productImageMapper.insert(image);
        }
    }

    private void deleteProductImages(Long productId) {
        LambdaQueryWrapper<FmProductImage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FmProductImage::getProductId, productId);
        productImageMapper.delete(wrapper);
    }

    private void fillStoreNames(List<ProductVO> voList) {
        if (voList == null || voList.isEmpty()) {
            return;
        }
        Set<Long> storeIds = voList.stream()
                .map(ProductVO::getStoreId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (storeIds.isEmpty()) {
            return;
        }

        List<FmStore> stores = storeMapper.selectBatchIds(storeIds);
        Map<Long, String> storeNameMap = stores.stream()
                .collect(Collectors.toMap(FmStore::getId, FmStore::getStoreName, (v1, v2) -> v1));

        for (ProductVO vo : voList) {
            if (vo.getStoreId() != null) {
                vo.setStoreName(storeNameMap.getOrDefault(vo.getStoreId(), "未知店铺 Unknown Store"));
            }
        }
    }
}
