package com.github.foli_backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.foli_backend.common.BusinessException;
import com.github.foli_backend.common.PageResult;
import com.github.foli_backend.constant.BizCodeEnum;
import com.github.foli_backend.constant.ComplaintStatusEnum;
import com.github.foli_backend.dto.request.ComplaintCreateRequest;
import com.github.foli_backend.dto.request.ComplaintHandleRequest;
import com.github.foli_backend.dto.response.ComplaintVO;
import com.github.foli_backend.entity.FmComplaint;
import com.github.foli_backend.entity.FmUser;
import com.github.foli_backend.helper.TestDataFactory;
import com.github.foli_backend.mapper.FmComplaintMapper;
import com.github.foli_backend.mapper.FmStoreMapper;
import com.github.foli_backend.mapper.FmUserMapper;
import com.github.foli_backend.service.impl.FmComplaintServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FmComplaintServiceImpl 单元测试")
class FmComplaintServiceImplTest {

    @Mock FmComplaintMapper complaintMapper;
    @Mock FmUserMapper userMapper;
    @Mock FmStoreMapper storeMapper;

    @InjectMocks
    FmComplaintServiceImpl complaintService;

    Long userId = 1L;
    Long storeId = 10L;

    @Nested
    @DisplayName("createComplaint — 创建投诉")
    class CreateComplaintTests {

        @Test
        @DisplayName("should_create_complaint_with_enriched_response")
        void shouldCreateComplaintWithEnrichedResponse() {
            when(complaintMapper.insert(any(FmComplaint.class))).thenReturn(1);
            FmUser user = TestDataFactory.createBuyer(userId, "buyer");
            when(userMapper.selectById(userId)).thenReturn(user);
            when(storeMapper.selectById(storeId)).thenReturn(null);

            ComplaintCreateRequest req = new ComplaintCreateRequest();
            req.setStoreId(storeId);
            req.setTitle("Bad product");
            req.setContent("Product is defective");

            ComplaintVO result = complaintService.createComplaint(userId, req);

            assertThat(result.getTitle()).isEqualTo("Bad product");
            assertThat(result.getStatus()).isEqualTo(ComplaintStatusEnum.PENDING.getCode());
            assertThat(result.getUserName()).isEqualTo("buyer_nick");
        }
    }

    @Nested
    @DisplayName("handleComplaint — 处理投诉")
    class HandleComplaintTests {

        @Test
        @DisplayName("should_handle_complaint_to_resolved_when_pending")
        void shouldHandleComplaintToResolved_whenPending() {
            FmComplaint complaint = new FmComplaint();
            complaint.setId(1L);
            complaint.setStatus(ComplaintStatusEnum.PENDING.getCode());
            when(complaintMapper.selectById(1L)).thenReturn(complaint);
            when(complaintMapper.updateById(any(FmComplaint.class))).thenReturn(1);

            ComplaintHandleRequest req = new ComplaintHandleRequest();
            req.setStatus(ComplaintStatusEnum.RESOLVED.getCode());

            complaintService.handleComplaint(1L, 1L, req);

            assertThat(complaint.getStatus()).isEqualTo(ComplaintStatusEnum.RESOLVED.getCode());
        }

        @Test
        @DisplayName("should_throw_already_handled_when_handling_resolved_complaint")
        void shouldThrowAlreadyHandled_whenHandlingResolvedComplaint() {
            FmComplaint complaint = new FmComplaint();
            complaint.setId(1L);
            complaint.setStatus(ComplaintStatusEnum.RESOLVED.getCode());
            when(complaintMapper.selectById(1L)).thenReturn(complaint);

            ComplaintHandleRequest req = new ComplaintHandleRequest();

            assertThatThrownBy(() -> complaintService.handleComplaint(1L, 1L, req))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code")
                    .isEqualTo(BizCodeEnum.COMPLAINT_ALREADY_HANDLED.getCode());
        }
    }

    @Nested
    @DisplayName("getComplaintDetail — 投诉详情")
    class GetComplaintDetailTests {

        @Test
        @DisplayName("should_throw_complaint_not_found_when_not_exist")
        void shouldThrowComplaintNotFound_whenNotExist() {
            when(complaintMapper.selectById(999L)).thenReturn(null);

            assertThatThrownBy(() -> complaintService.getComplaintDetail(999L))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code")
                    .isEqualTo(BizCodeEnum.COMPLAINT_NOT_FOUND.getCode());
        }
    }
}
