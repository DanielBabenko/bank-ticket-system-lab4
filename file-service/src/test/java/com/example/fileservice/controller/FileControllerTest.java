package com.example.fileservice.controller;

import com.example.fileservice.dto.ApplicationInfoDto;
import com.example.fileservice.dto.FileDto;
import com.example.fileservice.exception.BadRequestException;
import com.example.fileservice.exception.ForbiddenException;
import com.example.fileservice.exception.NotFoundException;
import com.example.fileservice.exception.UnauthorizedException;
import com.example.fileservice.service.FileService;
import io.minio.errors.MinioException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FileControllerTest {

    @Mock
    private FileService fileService;

    @InjectMocks
    private FileController fileController;

    private FileDto createSampleFileDto() {
        FileDto dto = new FileDto();
        dto.setId(UUID.randomUUID());
        dto.setOriginalName("test.txt");
        dto.setMimeType("text/plain");
        dto.setSize(1024L);
        dto.setExtension("txt");
        dto.setUploaderId(UUID.randomUUID());
        dto.setDescription("Test file");
        dto.setApplications(Collections.emptyList());
        return dto;
    }

    private MultipartFile createSampleMultipartFile() {
        return new MockMultipartFile("file", "test.txt", "text/plain", "test content".getBytes());
    }

    private Jwt createSampleJwt(UUID userId, String role) {
        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaimAsString("uid")).thenReturn(userId.toString());
        lenient().when(jwt.getClaims()).thenReturn(Map.of("role", role));
        return jwt;
    }

    // -----------------------
    // uploadFile tests
    // -----------------------
    @Test
    void uploadFile_validFile_returnsCreated() {
        MultipartFile file = createSampleMultipartFile();
        String description = "Test description";
        UUID userId = UUID.randomUUID();
        Jwt jwt = createSampleJwt(userId, "ROLE_CLIENT");
        FileDto responseDto = createSampleFileDto();

        when(fileService.uploadFile(any(MultipartFile.class), eq(userId), eq(description))).thenReturn(responseDto);

        UriComponentsBuilder uriBuilder = UriComponentsBuilder.newInstance();
        ResponseEntity<FileDto> response = fileController.uploadFile(file, description, jwt, uriBuilder);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(responseDto.getId(), response.getBody().getId());
        assertNotNull(response.getHeaders().getLocation());
        assertTrue(response.getHeaders().getLocation().toString().contains("/api/v1/files/"));
    }

    @Test
    void uploadFile_noUserIdInJwt_throwsIllegalArgumentException() {
        MultipartFile file = createSampleMultipartFile();
        String description = "Test";
        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaimAsString("uid")).thenReturn(null);

        UriComponentsBuilder uriBuilder = UriComponentsBuilder.newInstance();
        assertThrows(IllegalArgumentException.class, () ->
                fileController.uploadFile(file, description, jwt, uriBuilder)
        );
        verify(fileService, never()).uploadFile(any(), any(), any());
    }

    @Test
    void uploadFile_serviceThrowsBadRequest_throwsBadRequestException() {
        MultipartFile file = createSampleMultipartFile();
        String description = "Test";
        UUID userId = UUID.randomUUID();
        Jwt jwt = createSampleJwt(userId, "ROLE_CLIENT");

        when(fileService.uploadFile(any(MultipartFile.class), eq(userId), eq(description)))
                .thenThrow(new BadRequestException("Invalid file"));

        UriComponentsBuilder uriBuilder = UriComponentsBuilder.newInstance();
        assertThrows(BadRequestException.class, () ->
                fileController.uploadFile(file, description, jwt, uriBuilder)
        );
    }

    // -----------------------
    // downloadFile tests
    // -----------------------
    @Test
    void downloadFile_validIdAsUploader_returnsOkWithResource() {
        UUID fileId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Jwt jwt = createSampleJwt(userId, "ROLE_CLIENT");
        InputStream inputStream = new ByteArrayInputStream("test content".getBytes());
        FileService.InputStreamWithMetadata metadata = new FileService.InputStreamWithMetadata(
                inputStream, "test.txt", "text/plain", 12L
        );

        when(fileService.downloadFile(eq(fileId), eq(userId), eq(jwt))).thenReturn(metadata);

        ResponseEntity<InputStreamResource> response = fileController.downloadFile(fileId, jwt);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        HttpHeaders headers = response.getHeaders();
        assertEquals("attachment; filename=\"test.txt\"", headers.getFirst(HttpHeaders.CONTENT_DISPOSITION));
        assertEquals(MediaType.TEXT_PLAIN, headers.getContentType());
        assertEquals(12L, headers.getContentLength());
    }

    @Test
    void downloadFile_noUserIdInJwt_throwsIllegalArgumentException() {
        UUID fileId = UUID.randomUUID();
        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaimAsString("uid")).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () ->
                fileController.downloadFile(fileId, jwt)
        );
        verify(fileService, never()).downloadFile(any(), any(), any());
    }

    @Test
    void downloadFile_serviceThrowsNotFound_throwsNotFoundException() {
        UUID fileId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Jwt jwt = createSampleJwt(userId, "ROLE_CLIENT");

        when(fileService.downloadFile(eq(fileId), eq(userId), eq(jwt)))
                .thenThrow(new NotFoundException("File not found"));

        assertThrows(NotFoundException.class, () ->
                fileController.downloadFile(fileId, jwt)
        );
    }

    @Test
    void downloadFile_serviceThrowsForbidden_throwsForbiddenException() {
        UUID fileId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Jwt jwt = createSampleJwt(userId, "ROLE_CLIENT");

        when(fileService.downloadFile(eq(fileId), eq(userId), eq(jwt)))
                .thenThrow(new ForbiddenException("Access denied"));

        assertThrows(ForbiddenException.class, () ->
                fileController.downloadFile(fileId, jwt)
        );
    }

    // -----------------------
    // deleteFile tests
    // -----------------------
    @Test
    void deleteFile_validIdAsUploader_returnsNoContent() {
        UUID fileId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Jwt jwt = createSampleJwt(userId, "ROLE_CLIENT");

        doNothing().when(fileService).deleteFile(eq(fileId), eq(userId), eq(jwt));

        ResponseEntity<Void> response = fileController.deleteFile(fileId, jwt);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(fileService).deleteFile(eq(fileId), eq(userId), eq(jwt));
    }

    @Test
    void deleteFile_noUserIdInJwt_throwsIllegalArgumentException() {
        UUID fileId = UUID.randomUUID();
        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaimAsString("uid")).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () ->
                fileController.deleteFile(fileId, jwt)
        );
        verify(fileService, never()).deleteFile(any(), any(), any());
    }

    @Test
    void deleteFile_serviceThrowsNotFound_throwsNotFoundException() {
        UUID fileId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Jwt jwt = createSampleJwt(userId, "ROLE_CLIENT");

        doThrow(new NotFoundException("File not found")).when(fileService).deleteFile(eq(fileId), eq(userId), eq(jwt));

        assertThrows(NotFoundException.class, () ->
                fileController.deleteFile(fileId, jwt)
        );
    }

    @Test
    void deleteFile_serviceThrowsForbidden_throwsForbiddenException() {
        UUID fileId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Jwt jwt = createSampleJwt(userId, "ROLE_CLIENT");

        doThrow(new ForbiddenException("Access denied")).when(fileService).deleteFile(eq(fileId), eq(userId), eq(jwt));

        assertThrows(ForbiddenException.class, () ->
                fileController.deleteFile(fileId, jwt)
        );
    }

    @Test
    void deleteFile_serviceThrowsBadRequest_throwsBadRequestException() {
        UUID fileId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Jwt jwt = createSampleJwt(userId, "ROLE_CLIENT");

        doThrow(new BadRequestException("Cannot delete")).when(fileService).deleteFile(eq(fileId), eq(userId), eq(jwt));

        assertThrows(BadRequestException.class, () ->
                fileController.deleteFile(fileId, jwt)
        );
    }

    // -----------------------
    // listFiles tests
    // -----------------------
    @Test
    void listFiles_validParameters_returnsOkWithHeader() {
        FileDto dto1 = createSampleFileDto();
        FileDto dto2 = createSampleFileDto();
        Page<FileDto> page = new PageImpl<>(List.of(dto1, dto2), PageRequest.of(0, 20), 50);

        when(fileService.listAll(0, 20)).thenReturn(page);

        ResponseEntity<List<FileDto>> response = fileController.listFiles(0, 20);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals("50", response.getHeaders().getFirst("X-Total-Count"));
    }

    @Test
    void listFiles_sizeExceedsMax_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                fileController.listFiles(0, 51)
        );
        verify(fileService, never()).listAll(anyInt(), anyInt());
    }

    @Test
    void listFiles_emptyPage_returnsEmptyList() {
        Page<FileDto> page = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);

        when(fileService.listAll(0, 20)).thenReturn(page);

        ResponseEntity<List<FileDto>> response = fileController.listFiles(0, 20);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
        assertEquals("0", response.getHeaders().getFirst("X-Total-Count"));
    }

    // -----------------------
    // getFile tests
    // -----------------------
    @Test
    void getFile_validId_returnsOk() {
        UUID fileId = UUID.randomUUID();
        FileDto dto = createSampleFileDto();

        when(fileService.getFileById(eq(fileId))).thenReturn(dto);

        ResponseEntity<FileDto> response = fileController.getFile(fileId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(dto, response.getBody());
    }

    @Test
    void getFile_serviceThrowsNotFound_throwsNotFoundException() {
        UUID fileId = UUID.randomUUID();

        when(fileService.getFileById(eq(fileId))).thenThrow(new NotFoundException("File not found"));

        assertThrows(NotFoundException.class, () ->
                fileController.getFile(fileId)
        );
    }

    // -----------------------
    // getRealFileIds tests
    // -----------------------
    @Test
    void getRealFileIds_validIds_returnsOk() {
        List<UUID> fileIds = List.of(UUID.randomUUID(), UUID.randomUUID());
        List<UUID> realIds = List.of(fileIds.get(0));

        when(fileService.getFilesBatch(eq(fileIds))).thenReturn(realIds);

        ResponseEntity<List<UUID>> response = fileController.getRealFileIds(fileIds);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(realIds, response.getBody());
    }

    @Test
    void getRealFileIds_emptyList_returnsEmptyList() {
        List<UUID> fileIds = Collections.emptyList();

        ResponseEntity<List<UUID>> response = fileController.getRealFileIds(fileIds);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());
        verify(fileService, never()).getFilesBatch(any());
    }

    @Test
    void getRealFileIds_nullList_returnsEmptyList() {
        ResponseEntity<List<UUID>> response = fileController.getRealFileIds(null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());
        verify(fileService, never()).getFilesBatch(any());
    }

    // -----------------------
    // edge cases tests
    // -----------------------

    @Test
    void downloadFile_asAdmin_returnsOk() {
        UUID fileId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        Jwt jwt = createSampleJwt(adminId, "ROLE_ADMIN");
        InputStream inputStream = new ByteArrayInputStream("test content".getBytes());
        FileService.InputStreamWithMetadata metadata = new FileService.InputStreamWithMetadata(
                inputStream, "test.txt", "text/plain", 12L
        );

        when(fileService.downloadFile(eq(fileId), eq(adminId), eq(jwt))).thenReturn(metadata);

        ResponseEntity<InputStreamResource> response = fileController.downloadFile(fileId, jwt);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void deleteFile_asAdmin_returnsNoContent() {
        UUID fileId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        Jwt jwt = createSampleJwt(adminId, "ROLE_ADMIN");

        doNothing().when(fileService).deleteFile(eq(fileId), eq(adminId), eq(jwt));

        ResponseEntity<Void> response = fileController.deleteFile(fileId, jwt);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    void listFiles_largePageNumber_returnsEmptyIfNoData() {
        Page<FileDto> page = new PageImpl<>(List.of(), PageRequest.of(100, 20), 5);

        when(fileService.listAll(100, 20)).thenReturn(page);

        ResponseEntity<List<FileDto>> response = fileController.listFiles(100, 20);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
        assertEquals("5", response.getHeaders().getFirst("X-Total-Count"));
    }

    @Test
    void getFile_withApplications_returnsOk() {
        UUID fileId = UUID.randomUUID();
        FileDto dto = createSampleFileDto();
        dto.setApplications(List.of(new ApplicationInfoDto()));

        when(fileService.getFileById(eq(fileId))).thenReturn(dto);

        ResponseEntity<FileDto> response = fileController.getFile(fileId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().getApplications().size());
    }
}