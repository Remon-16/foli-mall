package com.github.foli_backend.service;

import com.github.foli_backend.common.BusinessException;
import com.github.foli_backend.constant.BizCodeEnum;
import com.github.foli_backend.service.impl.FileServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FileServiceImpl 单元测试")
class FileServiceImplTest {

    @InjectMocks
    FileServiceImpl fileService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(fileService, "uploadPath", tempDir.toString());
    }

    @Nested
    @DisplayName("upload — 文件上传")
    class UploadTests {

        @Test
        @DisplayName("should_throw_file_empty_when_file_is_null")
        void shouldThrowFileEmpty_whenFileIsNull() {
            assertThatThrownBy(() -> fileService.upload(null))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code")
                    .isEqualTo(BizCodeEnum.FILE_EMPTY.getCode());
        }

        @Test
        @DisplayName("should_throw_file_empty_when_file_is_empty")
        void shouldThrowFileEmpty_whenFileIsEmpty() {
            MultipartFile file = mock(MultipartFile.class);
            when(file.isEmpty()).thenReturn(true);

            assertThatThrownBy(() -> fileService.upload(file))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code")
                    .isEqualTo(BizCodeEnum.FILE_EMPTY.getCode());
        }

        @Test
        @DisplayName("should_throw_file_too_large_when_file_exceeds_10mb")
        void shouldThrowFileTooLarge_whenFileExceeds10MB() {
            MultipartFile file = mock(MultipartFile.class);
            when(file.isEmpty()).thenReturn(false);
            when(file.getSize()).thenReturn(11 * 1024 * 1024L);

            assertThatThrownBy(() -> fileService.upload(file))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code")
                    .isEqualTo(BizCodeEnum.FILE_TOO_LARGE.getCode());
        }

        @Test
        @DisplayName("should_throw_file_not_image_when_content_type_not_image")
        void shouldThrowFileNotImage_whenContentTypeNotImage() {
            MultipartFile file = mock(MultipartFile.class);
            when(file.isEmpty()).thenReturn(false);
            when(file.getSize()).thenReturn(1024L);
            when(file.getContentType()).thenReturn("text/plain");

            assertThatThrownBy(() -> fileService.upload(file))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code")
                    .isEqualTo(BizCodeEnum.FILE_NOT_IMAGE.getCode());
        }

        @Test
        @DisplayName("should_throw_file_not_image_when_content_type_is_null")
        void shouldThrowFileNotImage_whenContentTypeIsNull() {
            MultipartFile file = mock(MultipartFile.class);
            when(file.isEmpty()).thenReturn(false);
            when(file.getSize()).thenReturn(1024L);
            when(file.getContentType()).thenReturn(null);

            assertThatThrownBy(() -> fileService.upload(file))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code")
                    .isEqualTo(BizCodeEnum.FILE_NOT_IMAGE.getCode());
        }

        @Test
        @DisplayName("should_upload_successfully_and_return_url_when_valid_image")
        void shouldUploadSuccessfullyAndReturnUrl_whenValidImage() throws IOException {
            MultipartFile file = mock(MultipartFile.class);
            when(file.isEmpty()).thenReturn(false);
            when(file.getSize()).thenReturn(1024L);
            when(file.getContentType()).thenReturn("image/png");
            when(file.getOriginalFilename()).thenReturn("test.png");

            String result = fileService.upload(file);

            assertThat(result).startsWith("/uploads/images/");
            assertThat(result).endsWith(".png");
            // transferTo called to save the file
        }
    }
}
