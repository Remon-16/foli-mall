package com.github.foli_backend.common;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Collections;
import java.util.List;

/**
 * 分页响应包装 Paginated response wrapper
 * @param <T> 数据类型 data type
 */
@Schema(description = "分页结果 Paginated result")
public class PageResult<T> {

    @Schema(description = "总记录数 Total records")
    private long total;

    @Schema(description = "当前页码 Current page")
    private int page;

    @Schema(description = "每页大小 Page size")
    private int pageSize;

    @Schema(description = "当前页记录 Current page records")
    private List<T> records;

    public PageResult() {}

    public PageResult(long total, int page, int pageSize, List<T> records) {
        this.total = total;
        this.page = page;
        this.pageSize = pageSize;
        this.records = records != null ? records : Collections.emptyList();
    }

    /** 从 MyBatis-Plus Page 对象构建 build from MyBatis-Plus Page */
    public static <T> PageResult<T> of(com.baomidou.mybatisplus.extension.plugins.pagination.Page<T> mpPage) {
        return new PageResult<>(mpPage.getTotal(), (int) mpPage.getCurrent(), (int) mpPage.getSize(), mpPage.getRecords());
    }

    // getters & setters
    public long getTotal() { return total; }
    public void setTotal(long total) { this.total = total; }
    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }
    public int getPageSize() { return pageSize; }
    public void setPageSize(int pageSize) { this.pageSize = pageSize; }
    public List<T> getRecords() { return records; }
    public void setRecords(List<T> records) { this.records = records; }
}
