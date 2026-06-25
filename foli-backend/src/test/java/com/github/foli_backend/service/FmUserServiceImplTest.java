package com.github.foli_backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.foli_backend.common.BusinessException;
import com.github.foli_backend.constant.BizCodeEnum;
import com.github.foli_backend.constant.RoleConstants;
import com.github.foli_backend.entity.FmUser;
import com.github.foli_backend.helper.TestDataFactory;
import com.github.foli_backend.mapper.FmUserMapper;
import com.github.foli_backend.service.impl.FmUserServiceImpl;
import com.github.foli_backend.utils.JwtUtil;
import com.github.foli_backend.utils.PasswordUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FmUserServiceImpl 单元测试")
class FmUserServiceImplTest {

    @Mock FmUserMapper fmUserMapper;
    @Mock JwtUtil jwtUtil;

    @InjectMocks
    FmUserServiceImpl userService;

    // ==================== register ====================

    @Nested
    @DisplayName("register — 用户注册")
    class RegisterTests {

        @Test
        @DisplayName("should_register_successfully_when_username_not_exists")
        void shouldRegisterSuccessfully_whenUsernameNotExists() {
            when(fmUserMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
            when(fmUserMapper.insert(any(FmUser.class))).thenReturn(1);

            FmUser result = userService.register("newuser", "password123", "TestNick");

            assertThat(result.getUsername()).isEqualTo("newuser");
            assertThat(result.getNickname()).isEqualTo("TestNick");
            assertThat(result.getRole()).isEqualTo(RoleConstants.BUYER);
            assertThat(result.getStatus()).isEqualTo(1);
            assertThat(result.getBalance()).isEqualByComparingTo(java.math.BigDecimal.ZERO);
        }

        @Test
        @DisplayName("should_throw_username_exists_when_register_with_existing_username")
        void shouldThrowUsernameExists_whenRegisterWithExistingUsername() {
            FmUser existing = TestDataFactory.createBuyer(1L, "existing");
            when(fmUserMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(existing);

            assertThatThrownBy(() -> userService.register("existing", "pass", "nick"))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code")
                    .isEqualTo(BizCodeEnum.USERNAME_EXISTS.getCode());
        }

        @Test
        @DisplayName("should_use_username_as_nickname_when_nickname_is_null")
        void shouldUseUsernameAsNickname_whenNicknameIsNull() {
            when(fmUserMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
            when(fmUserMapper.insert(any(FmUser.class))).thenReturn(1);

            FmUser result = userService.register("user1", "pass", null);

            assertThat(result.getNickname()).isEqualTo("user1");
        }
    }

    // ==================== login ====================

    @Nested
    @DisplayName("login — 用户登录")
    class LoginTests {

        @Test
        @DisplayName("should_throw_invalid_credentials_when_username_not_found")
        void shouldThrowInvalidCredentials_whenUsernameNotFound() {
            when(fmUserMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

            assertThatThrownBy(() -> userService.login("nobody", "pass"))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code")
                    .isEqualTo(BizCodeEnum.INVALID_CREDENTIALS.getCode());
        }

        @Test
        @DisplayName("should_throw_account_disabled_when_status_is_null")
        void shouldThrowAccountDisabled_whenStatusIsNull() {
            FmUser user = TestDataFactory.createBuyer(1L, "buyer1");
            user.setStatus(null);
            when(fmUserMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(user);

            try (MockedStatic<PasswordUtil> mockedPwd = mockStatic(PasswordUtil.class)) {
                mockedPwd.when(() -> PasswordUtil.verify(anyString(), anyString())).thenReturn(true);

                assertThatThrownBy(() -> userService.login("buyer1", "pass"))
                        .isInstanceOf(BusinessException.class)
                        .extracting("code")
                        .isEqualTo(BizCodeEnum.ACCOUNT_DISABLED.getCode());
            }
        }

        @Test
        @DisplayName("should_throw_account_disabled_when_status_is_zero")
        void shouldThrowAccountDisabled_whenStatusIsZero() {
            FmUser user = TestDataFactory.createBuyer(1L, "buyer1");
            user.setStatus(0);
            when(fmUserMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(user);

            try (MockedStatic<PasswordUtil> mockedPwd = mockStatic(PasswordUtil.class)) {
                mockedPwd.when(() -> PasswordUtil.verify(anyString(), anyString())).thenReturn(true);

                assertThatThrownBy(() -> userService.login("buyer1", "pass"))
                        .isInstanceOf(BusinessException.class)
                        .extracting("code")
                        .isEqualTo(BizCodeEnum.ACCOUNT_DISABLED.getCode());
            }
        }
    }

    // ==================== getCurrentUser ====================

    @Nested
    @DisplayName("getCurrentUser — 获取当前用户")
    class GetCurrentUserTests {

        @Test
        @DisplayName("should_return_current_user_when_user_exists")
        void shouldReturnCurrentUser_whenUserExists() {
            FmUser user = TestDataFactory.createBuyer(1L, "buyer1");
            when(fmUserMapper.selectById(1L)).thenReturn(user);

            FmUser result = userService.getCurrentUser(1L);

            assertThat(result.getUsername()).isEqualTo("buyer1");
        }

        @Test
        @DisplayName("should_throw_user_not_found_when_get_current_user_with_invalid_id")
        void shouldThrowUserNotFound_whenGetCurrentUserWithInvalidId() {
            when(fmUserMapper.selectById(999L)).thenReturn(null);

            assertThatThrownBy(() -> userService.getCurrentUser(999L))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code")
                    .isEqualTo(BizCodeEnum.USER_NOT_FOUND.getCode());
        }
    }
}
