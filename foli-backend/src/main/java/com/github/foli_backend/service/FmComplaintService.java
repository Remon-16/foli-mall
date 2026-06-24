package com.github.foli_backend.service;

import com.github.foli_backend.common.PageResult;
import com.github.foli_backend.dto.request.ComplaintCreateRequest;
import com.github.foli_backend.dto.request.ComplaintHandleRequest;
import com.github.foli_backend.dto.response.ComplaintVO;

/**
 * 投诉服务接口 / Complaint service interface
 */
public interface FmComplaintService {

    /**
     * 创建投诉 Create complaint
     *
     * @param userId 投诉人用户ID complainant user ID
     * @param req    创建请求 create request
     * @return 投诉视图 complaint view object
     */
    ComplaintVO createComplaint(Long userId, ComplaintCreateRequest req);

    /**
     * 用户投诉列表 User's complaint list
     *
     * @param userId   用户ID user ID
     * @param page     页码 page number
     * @param pageSize 每页大小 page size
     * @return 分页结果 paginated result
     */
    PageResult<ComplaintVO> listUserComplaints(Long userId, int page, int pageSize);

    /**
     * 获取投诉详情 (含用户名称和店铺名称) Get complaint detail with user name and store name
     *
     * @param complaintId 投诉ID complaint ID
     * @return 投诉视图 complaint view object
     */
    ComplaintVO getComplaintDetail(Long complaintId);

    /**
     * 平台所有投诉列表 Admin: all complaints list
     *
     * @param page     页码 page number
     * @param pageSize 每页大小 page size
     * @param status   状态筛选 status filter (optional)
     * @return 分页结果 paginated result
     */
    PageResult<ComplaintVO> listAllComplaints(int page, int pageSize, Integer status);

    /**
     * 平台处理投诉 Admin: handle complaint
     *
     * @param complaintId 投诉ID complaint ID
     * @param handlerId   处理人ID handler ID
     * @param req         处理请求 handle request
     */
    void handleComplaint(Long complaintId, Long handlerId, ComplaintHandleRequest req);
}
