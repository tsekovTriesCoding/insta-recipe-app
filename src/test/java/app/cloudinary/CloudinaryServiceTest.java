package app.cloudinary;

import app.cloudinary.dto.ImageUploadResult;
import app.cloudinary.service.CloudinaryService;
import app.exception.ImageUploadException;
import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import com.cloudinary.utils.ObjectUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.lenient;

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
        lenient().when(cloudinary.uploader()).thenReturn(uploader); // Prevents the exception while still keeping the stubbing
    }

    @Test
    public void testUploadImageShouldReturnImageUploadResult() throws Exception {
        MultipartFile mockFile = new MockMultipartFile("file", "test.jpg", "image/jpeg", "dummy data".getBytes());
        Map<String, Object> fakeUploadResult = Map.of(
                "secure_url", "https://res.cloudinary.com/demo/image/upload/sample.jpg",
                "public_id", "sample_public_id"
        );

        Mockito.when(uploader.upload(Mockito.any(byte[].class), Mockito.anyMap()))
                .thenReturn(fakeUploadResult);

        ImageUploadResult result = cloudinaryService.uploadImage(mockFile);

        assertNotNull(result);
        assertEquals("https://res.cloudinary.com/demo/image/upload/sample.jpg", result.getImageUrl());
        assertEquals("sample_public_id", result.getPublicId());
    }

    @Test
    public void testUploadImageShouldThrowExceptionWhenUploadFails() throws Exception {
        MultipartFile mockFile = new MockMultipartFile("file", "test.jpg", "image/jpeg", "dummy data".getBytes());

        Mockito.when(uploader.upload(Mockito.any(byte[].class), Mockito.anyMap()))
                .thenThrow(new IOException("Cloudinary upload failed"));

        assertThrows(ImageUploadException.class, () -> cloudinaryService.uploadImage(mockFile));
    }

    @Test
    public void testDeleteImageShouldCallCloudinaryDestroy() throws Exception {
        String publicId = "test_public_id";

        cloudinaryService.deleteImage(publicId);

        Mockito.verify(uploader, Mockito.times(1))
                .destroy(publicId, ObjectUtils.emptyMap());
    }

    @Test
    public void testDeleteImageShouldDoNothingWhenPublicIdIsNullOrEmpty() {
        cloudinaryService.deleteImage(null);
        cloudinaryService.deleteImage("");

        Mockito.verifyNoInteractions(uploader);
    }

    @Test
    public void testDeleteImageShouldThrowExceptionWhenDeletionFails() throws Exception {
        String publicId = "test_public_id";

        Mockito.when(uploader.destroy(Mockito.anyString(), Mockito.anyMap()))
                .thenThrow(new IOException("Cloudinary deletion failed"));

        assertThrows(ImageUploadException.class, () -> cloudinaryService.deleteImage(publicId));
    }
}