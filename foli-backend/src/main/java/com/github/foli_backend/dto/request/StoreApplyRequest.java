package com.github.foli_backend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * 店铺申请请求 DTO
 * Store application request DTO
 * 用于创建和更新店铺时接收表单数据
 * Used to receive form data when creating or updating a store
 */
@Schema(description = "店铺申请请求 Store application request")
public class StoreApplyRequest {

    /** 店铺名称 store name */
    @NotBlank(message = "店铺名称不能为空 Store name cannot be blank")
    @Schema(description = "店铺名称 Store name", example = "我的小店")
    private String storeName;

    /** 店铺Logo图片URL store logo image URL */
    @Schema(description = "店铺Logo Store logo URL", example = "https://img.example.com/logo.jpg")
    private String storeLogo;

    /** 店铺简介 store description */
    @Schema(description = "店铺简介 Store description", example = "本店主要经营电子产品")
    private String description;

    // ========== Getters and Setters ==========

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public String getStoreLogo() {
        return storeLogo;
    }

    public void setStoreLogo(String storeLogo) {
        this.storeLogo = storeLogo;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
