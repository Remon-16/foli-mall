package com.github.foli_backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.foli_backend.common.BusinessException;
import com.github.foli_backend.constant.BizCodeEnum;
import com.github.foli_backend.entity.FmProductCategory;
import com.github.foli_backend.mapper.FmProductCategoryMapper;
import com.github.foli_backend.service.impl.FmProductCategoryServiceImpl;
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
@DisplayName("FmProductCategoryServiceImpl 单元测试")
class FmProductCategoryServiceImplTest {

    @Mock FmProductCategoryMapper fmProductCategoryMapper;

    @InjectMocks
    FmProductCategoryServiceImpl categoryService;

    @Nested
    @DisplayName("getTree — 获取分类树")
    class GetTreeTests {

        @Test
        @DisplayName("should_return_category_tree_when_categories_exist")
        void shouldReturnCategoryTree_whenCategoriesExist() {
            FmProductCategory cat = new FmProductCategory();
            cat.setId(1L);
            cat.setParentId(0L);
            cat.setName("Electronics");
            cat.setSortOrder(1);
            cat.setStatus(1);

            when(fmProductCategoryMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(cat));

            var result = categoryService.getTree();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getName()).isEqualTo("Electronics");
        }

        @Test
        @DisplayName("should_return_empty_list_when_no_categories")
        void shouldReturnEmptyList_whenNoCategories() {
            when(fmProductCategoryMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());

            var result = categoryService.getTree();

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("delete — 删除分类")
    class DeleteTests {

        @Test
        @DisplayName("should_throw_category_has_children_when_delete_with_children")
        void shouldThrowCategoryHasChildren_whenDeleteWithChildren() {
            FmProductCategory cat = new FmProductCategory();
            cat.setId(1L);
            when(fmProductCategoryMapper.selectById(1L)).thenReturn(cat);
            when(fmProductCategoryMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(3L);

            assertThatThrownBy(() -> categoryService.delete(1L))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code")
                    .isEqualTo(BizCodeEnum.CATEGORY_HAS_CHILDREN.getCode());
        }

        @Test
        @DisplayName("should_delete_successfully_when_no_children")
        void shouldDeleteSuccessfully_whenNoChildren() {
            FmProductCategory cat = new FmProductCategory();
            cat.setId(1L);
            when(fmProductCategoryMapper.selectById(1L)).thenReturn(cat);
            when(fmProductCategoryMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
            when(fmProductCategoryMapper.deleteById(1L)).thenReturn(1);

            categoryService.delete(1L);

            verify(fmProductCategoryMapper).deleteById(1L);
        }
    }

    @Nested
    @DisplayName("update — 更新分类")
    class UpdateTests {

        @Test
        @DisplayName("should_throw_category_parent_self_when_parent_id_equals_id")
        void shouldThrowCategoryParentSelf_whenParentIdEqualsId() {
            FmProductCategory cat = new FmProductCategory();
            cat.setId(1L);
            when(fmProductCategoryMapper.selectById(1L)).thenReturn(cat);

            assertThatThrownBy(() -> categoryService.update(1L, "Name", 1L, null, null))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code")
                    .isEqualTo(BizCodeEnum.CATEGORY_PARENT_SELF.getCode());
        }
    }
}
