package app.cloudinary;

import app.exception.ImageUploadException;
import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import com.cloudinary.utils.ObjectUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CloudinaryServiceTest {

    @Mock
    private Cloudinary cloudinary;

    @Mock
    private Uploader uploader;

    @InjectMocks
    private CloudinaryService cloudinaryService;

    @BeforeEach
    void setUp() {
        when(cloudinary.uploader()).thenReturn(uploader);
    }

    @Test
    void uploadImageShouldReturnSecureUrlWhenUploadIsSuccessful() throws Exception {
        // Arrange: Create a mock file
        MockMultipartFile file = new MockMultipartFile("image", "test.jpg", "image/jpeg", "test data".getBytes());

        Map<String, String> fakeUploadResult = Map.of("secure_url", "https://cloudinary.com/test.jpg");
        when(uploader.upload(any(byte[].class), eq(ObjectUtils.emptyMap()))).thenReturn(fakeUploadResult);

        // Act: Call the upload method
        String imageUrl = cloudinaryService.uploadImage(file);

        // Assert: Verify the URL is returned correctly
        assertThat(imageUrl).isEqualTo("https://cloudinary.com/test.jpg");
    }

    @Test
    void uploadImageShouldThrowExceptionWhenUploadFails() throws Exception {
        MockMultipartFile file = new MockMultipartFile("image", "test.jpg", "image/jpeg", "test data".getBytes());

        when(uploader.upload(any(byte[].class), eq(ObjectUtils.emptyMap()))).thenThrow(new IOException());

        assertThatThrownBy(() -> cloudinaryService.uploadImage(file))
                .isInstanceOf(ImageUploadException.class)
                .hasMessage("Failed to upload image to Cloudinary");
    }
}