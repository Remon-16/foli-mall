package com.github.foli_backend.service;

import com.github.foli_backend.common.PageResult;
import com.github.foli_backend.dto.request.ReturnCreateRequest;
import com.github.foli_backend.dto.request.ReturnDisputeRequest;
import com.github.foli_backend.dto.request.ReturnReviewRequest;
import com.github.foli_backend.dto.response.ReturnRefundVO;

/**
 * 退货退款服务接口 / Return/refund service interface
 */
public interface FmReturnRefundService {

    /**
     * 创建退货退款申请 Create return/refund request
     *
     * @param userId 买家用户ID buyer user ID
     * @param req    创建请求 create request
     * @return 退货退款视图 return/refund view object
     */
    ReturnRefundVO createReturn(Long userId, ReturnCreateRequest req);

    /**
     * 买家退货列表 Buyer's return list
     *
     * @param userId   用户ID user ID
     * @param page     页码 page number
     * @param pageSize 每页大小 page size
     * @return 分页结果 paginated result
     */
    PageResult<ReturnRefundVO> listBuyerReturns(Long userId, int page, int pageSize);

    /**
     * 获取退货详情 Get return detail
     *
     * @param returnId 退货ID return ID
     * @return 退货退款视图 return/refund view object
     */
    ReturnRefundVO getReturnDetail(Long returnId);

    /**
     * 买家退回商品 (更新状态为BUYER_SHIPPING) Ship back goods
     *
     * @param returnId 退货ID return ID
     * @param userId   用户ID user ID
     */
    void shipBack(Long returnId, Long userId);

    /**
     * 卖家退货列表 Seller's return list
     *
     * @param storeId  店铺ID store ID
     * @param page     页码 page number
     * @param pageSize 每页大小 page size
     * @param status   状态筛选 status filter (optional)
     * @return 分页结果 paginated result
     */
    PageResult<ReturnRefundVO> listStoreReturns(Long storeId, int page, int pageSize, Integer status);

    /**
     * 卖家审核退货申请 (通过/拒绝) Review return request (approve/reject)
     *
     * @param returnId 退货ID return ID
     * @param storeId  店铺ID store ID
     * @param req      审核请求 review request
     */
    void reviewReturn(Long returnId, Long storeId, ReturnReviewRequest req);

    /**
     * 卖家确认收货 Confirm receipt of returned goods
     *
     * @param returnId 退货ID return ID
     * @param storeId  店铺ID store ID
     */
    void confirmReceipt(Long returnId, Long storeId);

    /**
     * 卖家验货通过 退款给买家 Inspect passed, refund to buyer
     *
     * @param returnId 退货ID return ID
     * @param storeId  店铺ID store ID
     */
    void inspectPass(Long returnId, Long storeId);

    /**
     * 卖家验货不通过 进入争议 Dispute, auto-create complaint
     *
     * @param returnId 退货ID return ID
     * @param storeId  店铺ID store ID
     * @param req      争议请求 dispute request
     */
    void dispute(Long returnId, Long storeId, ReturnDisputeRequest req);

    /**
     * 平台所有退货列表 Admin: all returns list
     *
     * @param page     页码 page number
     * @param pageSize 每页大小 page size
     * @param status   状态筛选 status filter (optional)
     * @return 分页结果 paginated result
     */
    PageResult<ReturnRefundVO> listAllReturns(int page, int pageSize, Integer status);

    /**
     * 平台仲裁争议 Admin: handle dispute
     *
     * @param returnId 退货ID return ID
     * @param result   处理结果 handle result
     */
    void handleDispute(Long returnId, String result);

    /**
     * 买家争议申诉（卖家驳回后）Buyer disputes rejected return
     *
     * @param returnId 退货ID return ID
     * @param userId   买家用户ID buyer user ID
     * @param reason   争议原因 dispute reason
     */
    void buyerDispute(Long returnId, Long userId, String reason);
}
