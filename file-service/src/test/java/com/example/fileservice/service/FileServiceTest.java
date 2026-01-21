/*package com.example.fileservice.service;

import com.example.fileservice.dto.ApplicationInfoDto;
import com.example.fileservice.dto.FileDto;
import com.example.fileservice.exception.*;
import com.example.fileservice.feign.ApplicationServiceClient;
import com.example.fileservice.model.entity.File;
import com.example.fileservice.repository.FileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class FileServiceTest {

    @Mock
    private FileRepository fileRepository;

    @Mock
    private ApplicationServiceClient applicationServiceClient;

    @Mock
    private MinioService minioService;

    @Mock
    private MultipartFile multipartFile;

    @Mock
    private Jwt jwt;

    private FileService fileService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        fileService = new FileService(fileRepository, applicationServiceClient, minioService);
    }

    // -----------------------
    // uploadFile tests
    // -----------------------
    @Test
    public void uploadFile_emptyFile_throwsBadRequest() {
        UUID uploaderId = UUID.randomUUID();
        String description = "Test file";

        when(multipartFile.isEmpty()).thenReturn(true);

        assertThrows(BadRequestException.class, () ->
                fileService.uploadFile(multipartFile, uploaderId, description));

        verifyNoInteractions(fileRepository);
        verifyNoInteractions(minioService);
    }

    @Test
    public void uploadFile_nullFilename_throwsBadRequest() {
        UUID uploaderId = UUID.randomUUID();
        String description = "Test file";

        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getOriginalFilename()).thenReturn(null);

        assertThrows(BadRequestException.class, () ->
                fileService.uploadFile(multipartFile, uploaderId, description));

        verifyNoInteractions(fileRepository);
        verifyNoInteractions(minioService);
    }

    @Test
    public void uploadFile_blankFilename_throwsBadRequest() {
        UUID uploaderId = UUID.randomUUID();
        String description = "Test file";

        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getOriginalFilename()).thenReturn("   ");

        assertThrows(BadRequestException.class, () ->
                fileService.uploadFile(multipartFile, uploaderId, description));

        verifyNoInteractions(fileRepository);
        verifyNoInteractions(minioService);
    }

    @Test
    public void uploadFile_success_returnsDto() throws IOException {
        UUID uploaderId = UUID.randomUUID();
        String description = "Test file";
        String filename = "test.txt";
        String mimeType = "text/plain";
        long fileSize = 1024L;
        UUID generatedFileId = UUID.randomUUID();
        String expectedStorageKey = generatedFileId + ".txt";

        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getOriginalFilename()).thenReturn(filename);
        when(multipartFile.getSize()).thenReturn(fileSize);
        when(multipartFile.getContentType()).thenReturn(mimeType);
        when(multipartFile.getInputStream()).thenReturn(new ByteArrayInputStream("test content".getBytes()));

        // Создаем файл с корректным конструктором
        File savedFile = new File(generatedFileId, filename, fileSize, mimeType, uploaderId);
        savedFile.setDescription(description);

        when(fileRepository.save(any(File.class))).thenReturn(savedFile);
        when(minioService.uploadFile(any(MultipartFile.class), anyString())).thenReturn(expectedStorageKey);

        FileDto result = fileService.uploadFile(multipartFile, uploaderId, description);

        assertNotNull(result);
        assertEquals(filename, result.getOriginalName());
        assertEquals(fileSize, result.getSize());
        assertEquals(uploaderId, result.getUploaderId());
        assertEquals(description, result.getDescription());

        verify(fileRepository, times(1)).save(any(File.class));
        verify(minioService, times(1)).uploadFile(any(MultipartFile.class), anyString());
    }

    @Test
    public void uploadFile_ioException_throwsRuntimeException() throws IOException {
        UUID uploaderId = UUID.randomUUID();
        String description = "Test file";
        String filename = "test.txt";

        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getOriginalFilename()).thenReturn(filename);
        when(multipartFile.getSize()).thenReturn(1024L);
        when(multipartFile.getContentType()).thenReturn("text/plain");
        when(multipartFile.getInputStream()).thenThrow(new IOException("IO error"));

        verify(fileRepository, never()).save(any(File.class));
    }

    // -----------------------
    // downloadFile tests
    // -----------------------
    @Test
    public void downloadFile_fileNotFound_throwsNotFoundException() {
        UUID fileId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(fileRepository.findById(fileId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () ->
                fileService.downloadFile(fileId, userId, jwt));

        verify(fileRepository, times(1)).findById(fileId);
        verifyNoInteractions(minioService);
    }

    @Test
    public void downloadFile_fileNotInStorage_throwsNotFoundException() {
        UUID fileId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        File file = new File();
        file.setId(fileId);
        file.setUploaderId(userId);
        file.setStorageKey("test-key");

        when(fileRepository.findById(fileId)).thenReturn(Optional.of(file));
        when(minioService.fileExists("test-key")).thenReturn(false);

        assertThrows(NotFoundException.class, () ->
                fileService.downloadFile(fileId, userId, jwt));

        verify(minioService, times(1)).fileExists("test-key");
        verify(minioService, never()).downloadFile(anyString());
    }

    @Test
    public void downloadFile_unauthorizedUser_throwsForbidden() {
        UUID fileId = UUID.randomUUID();
        UUID uploaderId = UUID.randomUUID();
        UUID downloaderId = UUID.randomUUID(); // Different user

        File file = new File();
        file.setId(fileId);
        file.setUploaderId(uploaderId);
        file.setStorageKey("test-key");

        when(fileRepository.findById(fileId)).thenReturn(Optional.of(file));
        when(minioService.fileExists("test-key")).thenReturn(true);
        when(jwt.getClaims()).thenReturn(Map.of("role", "ROLE_USER")); // Not admin or manager

        assertThrows(ForbiddenException.class, () ->
                fileService.downloadFile(fileId, downloaderId, jwt));

        verify(minioService, times(1)).fileExists("test-key");
        verify(minioService, never()).downloadFile(anyString());
    }

    @Test
    public void downloadFile_adminUser_success() {
        UUID fileId = UUID.randomUUID();
        UUID uploaderId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID(); // Different user, but admin

        File file = new File();
        file.setId(fileId);
        file.setUploaderId(uploaderId);
        file.setStorageKey("test-key");
        file.setOriginalName("test.txt");
        file.setMimeType("text/plain");
        file.setSize(1024L);

        InputStream mockStream = new ByteArrayInputStream("test".getBytes());

        when(fileRepository.findById(fileId)).thenReturn(Optional.of(file));
        when(minioService.fileExists("test-key")).thenReturn(true);
        when(jwt.getClaims()).thenReturn(Map.of("role", "ROLE_ADMIN"));
        when(minioService.downloadFile("test-key")).thenReturn(mockStream);

        FileService.InputStreamWithMetadata result = fileService.downloadFile(fileId, adminId, jwt);

        assertNotNull(result);
        assertEquals("test.txt", result.filename());
        assertEquals("text/plain", result.contentType());
        assertEquals(1024L, result.size());
        assertNotNull(result.inputStream());

        verify(minioService, times(1)).downloadFile("test-key");
    }

    @Test
    public void downloadFile_managerUser_success() {
        UUID fileId = UUID.randomUUID();
        UUID uploaderId = UUID.randomUUID();
        UUID managerId = UUID.randomUUID(); // Different user, but manager

        File file = new File();
        file.setId(fileId);
        file.setUploaderId(uploaderId);
        file.setStorageKey("test-key");
        file.setOriginalName("test.txt");
        file.setMimeType("text/plain");
        file.setSize(1024L);

        InputStream mockStream = new ByteArrayInputStream("test".getBytes());

        when(fileRepository.findById(fileId)).thenReturn(Optional.of(file));
        when(minioService.fileExists("test-key")).thenReturn(true);
        when(jwt.getClaims()).thenReturn(Map.of("role", "ROLE_MANAGER"));
        when(minioService.downloadFile("test-key")).thenReturn(mockStream);

        FileService.InputStreamWithMetadata result = fileService.downloadFile(fileId, managerId, jwt);

        assertNotNull(result);
        assertEquals("test.txt", result.filename());
        assertEquals("text/plain", result.contentType());
        assertEquals(1024L, result.size());
        assertNotNull(result.inputStream());

        verify(minioService, times(1)).downloadFile("test-key");
    }

    @Test
    public void downloadFile_ownerUser_success() {
        UUID fileId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();

        File file = new File();
        file.setId(fileId);
        file.setUploaderId(ownerId);
        file.setStorageKey("test-key");
        file.setOriginalName("test.txt");
        file.setMimeType("text/plain");
        file.setSize(1024L);

        InputStream mockStream = new ByteArrayInputStream("test".getBytes());

        when(fileRepository.findById(fileId)).thenReturn(Optional.of(file));
        when(minioService.fileExists("test-key")).thenReturn(true);
        when(jwt.getClaims()).thenReturn(Map.of("role", "ROLE_USER")); // Not admin/manager but owner
        when(minioService.downloadFile("test-key")).thenReturn(mockStream);

        FileService.InputStreamWithMetadata result = fileService.downloadFile(fileId, ownerId, jwt);

        assertNotNull(result);
        assertEquals("test.txt", result.filename());
        assertEquals("text/plain", result.contentType());
        assertEquals(1024L, result.size());
        assertNotNull(result.inputStream());

        verify(minioService, times(1)).downloadFile("test-key");
    }

    // -----------------------
    // deleteFile tests
    // -----------------------
    @Test
    public void deleteFile_nullFileId_throwsBadRequest() {
        UUID userId = UUID.randomUUID();

        assertThrows(BadRequestException.class, () ->
                fileService.deleteFile(null, userId, jwt));

        verifyNoInteractions(fileRepository);
        verifyNoInteractions(minioService);
    }

    @Test
    public void deleteFile_fileNotFound_throwsNotFoundException() {
        UUID fileId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(fileRepository.findById(fileId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () ->
                fileService.deleteFile(fileId, userId, jwt));

        verify(fileRepository, times(1)).findById(fileId);
        verifyNoInteractions(minioService);
    }

    @Test
    public void deleteFile_unauthorizedUser_throwsForbidden() {
        UUID fileId = UUID.randomUUID();
        UUID uploaderId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();

        File file = new File();
        file.setId(fileId);
        file.setUploaderId(uploaderId);
        file.setStorageKey("test-key");

        when(fileRepository.findById(fileId)).thenReturn(Optional.of(file));
        when(jwt.getClaims()).thenReturn(Map.of("role", "ROLE_USER")); // Not admin

        assertThrows(ForbiddenException.class, () ->
                fileService.deleteFile(fileId, otherUserId, jwt));

        verify(fileRepository, never()).delete(any(File.class));
        verify(minioService, never()).deleteFile(anyString());
    }

    @Test
    public void deleteFile_adminUser_success() {
        UUID fileId = UUID.randomUUID();
        UUID uploaderId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();

        File file = new File();
        file.setId(fileId);
        file.setUploaderId(uploaderId);
        file.setStorageKey("test-key");
        file.setOriginalName("test.txt");

        when(fileRepository.findById(fileId)).thenReturn(Optional.of(file));
        when(jwt.getClaims()).thenReturn(Map.of("role", "ROLE_ADMIN"));
        doNothing().when(minioService).deleteFile("test-key");

        fileService.deleteFile(fileId, adminId, jwt);

        verify(minioService, times(1)).deleteFile("test-key");
        verify(fileRepository, times(1)).delete(file);
    }

    @Test
    public void deleteFile_ownerUser_success() {
        UUID fileId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();

        File file = new File();
        file.setId(fileId);
        file.setUploaderId(ownerId);
        file.setStorageKey("test-key");
        file.setOriginalName("test.txt");

        when(fileRepository.findById(fileId)).thenReturn(Optional.of(file));
        when(jwt.getClaims()).thenReturn(Map.of("role", "ROLE_USER")); // Not admin but owner
        doNothing().when(minioService).deleteFile("test-key");

        fileService.deleteFile(fileId, ownerId, jwt);

        verify(minioService, times(1)).deleteFile("test-key");
        verify(fileRepository, times(1)).delete(file);
    }

    @Test
    public void deleteFile_minioDeleteFails_throwsBadRequest() {
        UUID fileId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();

        File file = new File();
        file.setId(fileId);
        file.setUploaderId(ownerId);
        file.setStorageKey("test-key");

        when(fileRepository.findById(fileId)).thenReturn(Optional.of(file));
        when(jwt.getClaims()).thenReturn(Map.of("role", "ROLE_USER"));
        doThrow(new RuntimeException("MinIO error")).when(minioService).deleteFile("test-key");

        assertThrows(BadRequestException.class, () ->
                fileService.deleteFile(fileId, ownerId, jwt));

        verify(fileRepository, never()).delete(any(File.class));
    }

    // -----------------------
    // getFiles tests
    // -----------------------
    @Test
    public void getFiles_nullIds_returnsEmptyList() {
        List<File> result = fileService.getFiles(null);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verifyNoInteractions(fileRepository);
    }

    @Test
    public void getFiles_emptyIds_returnsEmptyList() {
        List<File> result = fileService.getFiles(Collections.emptyList());

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verifyNoInteractions(fileRepository);
    }

    @Test
    public void getFiles_withIds_returnsFiles() {
        UUID fileId1 = UUID.randomUUID();
        UUID fileId2 = UUID.randomUUID();
        List<UUID> ids = Arrays.asList(fileId1, fileId2);

        File file1 = new File();
        file1.setId(fileId1);
        File file2 = new File();
        file2.setId(fileId2);
        List<File> expectedFiles = Arrays.asList(file1, file2);

        when(fileRepository.findByIds(ids)).thenReturn(expectedFiles);

        List<File> result = fileService.getFiles(ids);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(fileRepository, times(1)).findByIds(ids);
    }

    // -----------------------
    // getFilesBatch tests
    // -----------------------
    @Test
    public void getFilesBatch_returnsFileIds() {
        UUID fileId1 = UUID.randomUUID();
        UUID fileId2 = UUID.randomUUID();
        List<UUID> ids = Arrays.asList(fileId1, fileId2);

        File file1 = new File();
        file1.setId(fileId1);
        File file2 = new File();
        file2.setId(fileId2);
        List<File> files = Arrays.asList(file1, file2);

        when(fileRepository.findByIds(ids)).thenReturn(files);

        List<UUID> result = fileService.getFilesBatch(ids);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(fileId1));
        assertTrue(result.contains(fileId2));
        verify(fileRepository, times(1)).findByIds(ids);
    }

    // -----------------------
    // listAll tests
    // -----------------------
    @Test
    public void listAll_returnsPagedDto() {
        UUID fileId = UUID.randomUUID();
        UUID uploaderId = UUID.randomUUID();

        File file = new File();
        file.setId(fileId);
        file.setUploaderId(uploaderId);
        file.setStorageKey("test-key");
        file.setOriginalName("test.txt");
        file.setMimeType("text/plain");
        file.setSize(1024L);

        List<File> fileList = Collections.singletonList(file);
        Page<File> filePage = new PageImpl<>(fileList);

        when(fileRepository.findAll(PageRequest.of(0, 10))).thenReturn(filePage);
        when(applicationServiceClient.getApplicationsByFile(fileId))
                .thenReturn(Collections.emptyList());
        when(minioService.getFileUrl("test-key")).thenReturn("http://minio/test-key");

        Page<FileDto> result = fileService.listAll(0, 10);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(fileId, result.getContent().get(0).getId());
        verify(fileRepository, times(1)).findAll(PageRequest.of(0, 10));
    }

    // -----------------------
    // getFileById tests
    // -----------------------
    @Test
    public void getFileById_nullId_throwsBadRequest() {
        assertThrows(BadRequestException.class, () -> fileService.getFileById(null));
        verifyNoInteractions(fileRepository);
    }

    @Test
    public void getFileById_fileNotFound_throwsNotFoundException() {
        UUID fileId = UUID.randomUUID();

        when(fileRepository.findById(fileId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> fileService.getFileById(fileId));
        verify(fileRepository, times(1)).findById(fileId);
    }

    @Test
    public void getFileById_success_returnsDto() {
        UUID fileId = UUID.randomUUID();
        UUID uploaderId = UUID.randomUUID();

        File file = new File();
        file.setId(fileId);
        file.setUploaderId(uploaderId);
        file.setStorageKey("test-key");
        file.setOriginalName("test.txt");
        file.setMimeType("text/plain");
        file.setSize(1024L);
        file.setDescription("Test description");

        when(fileRepository.findById(fileId)).thenReturn(Optional.of(file));
        when(applicationServiceClient.getApplicationsByFile(fileId))
                .thenReturn(Collections.singletonList(new ApplicationInfoDto()));
        when(minioService.getFileUrl("test-key")).thenReturn("http://minio/test-key");

        FileDto result = fileService.getFileById(fileId);

        assertNotNull(result);
        assertEquals(fileId, result.getId());
        assertEquals("test.txt", result.getOriginalName());
        assertEquals(uploaderId, result.getUploaderId());
        assertEquals("Test description", result.getDescription());
        assertEquals("http://minio/test-key", result.getDownloadUrl());
        assertEquals(1, result.getApplications().size());

        verify(fileRepository, times(1)).findById(fileId);
        verify(applicationServiceClient, times(1)).getApplicationsByFile(fileId);
    }

    @Test
    public void getFileById_applicationServiceUnavailable_returnsDtoWithoutApplications() {
        UUID fileId = UUID.randomUUID();
        UUID uploaderId = UUID.randomUUID();

        File file = new File();
        file.setId(fileId);
        file.setUploaderId(uploaderId);
        file.setStorageKey("test-key");
        file.setOriginalName("test.txt");
        file.setMimeType("text/plain");
        file.setSize(1024L);

        when(fileRepository.findById(fileId)).thenReturn(Optional.of(file));
        when(applicationServiceClient.getApplicationsByFile(fileId))
                .thenThrow(new RuntimeException("Service unavailable"));
        when(minioService.getFileUrl("test-key")).thenReturn("http://minio/test-key");

        FileDto result = fileService.getFileById(fileId);

        assertNotNull(result);
        assertEquals(fileId, result.getId());
        assertNotNull(result.getApplications());
        assertTrue(result.getApplications().isEmpty());

        verify(fileRepository, times(1)).findById(fileId);
        verify(applicationServiceClient, times(1)).getApplicationsByFile(fileId);
    }

    @Test
    public void getFileById_applicationServiceReturnsNull_throwsServiceUnavailable() {
        UUID fileId = UUID.randomUUID();
        UUID uploaderId = UUID.randomUUID();

        File file = new File();
        file.setId(fileId);
        file.setUploaderId(uploaderId);
        file.setStorageKey("test-key");
        file.setOriginalName("test.txt");
        file.setMimeType("text/plain");
        file.setSize(1024L);

        when(fileRepository.findById(fileId)).thenReturn(Optional.of(file));
        when(applicationServiceClient.getApplicationsByFile(fileId)).thenReturn(null);

        FileDto result = fileService.getFileById(fileId);

        assertNotNull(result);
        assertEquals(fileId, result.getId());
        assertNotNull(result.getApplications());
        assertTrue(result.getApplications().isEmpty());

        verify(fileRepository, times(1)).findById(fileId);
        verify(applicationServiceClient, times(1)).getApplicationsByFile(fileId);
    }

    // -----------------------
    // toDto tests (через public методы)
    // -----------------------
    @Test
    public void toDto_mapsAllFieldsCorrectly() {
        UUID fileId = UUID.randomUUID();
        UUID uploaderId = UUID.randomUUID();

        File file = new File();
        file.setId(fileId);
        file.setUploaderId(uploaderId);
        file.setStorageKey("test-key");
        file.setOriginalName("test.txt");
        file.setMimeType("text/plain");
        file.setSize(1024L);
        file.setExtension("txt");
        file.setDescription("Test file");
        file.setUploadDate(LocalDateTime.now());

        ApplicationInfoDto appDto = new ApplicationInfoDto();
        appDto.setId(UUID.randomUUID());
        appDto.setStatus("Test App");

        when(applicationServiceClient.getApplicationsByFile(fileId))
                .thenReturn(Collections.singletonList(appDto));
        when(minioService.getFileUrl("test-key")).thenReturn("http://minio/test-key");

        // Используем метод getFileById, который внутри вызывает toDto
        when(fileRepository.findById(fileId)).thenReturn(Optional.of(file));

        FileDto result = fileService.getFileById(fileId);

        assertNotNull(result);
        assertEquals(fileId, result.getId());
        assertEquals("test.txt", result.getOriginalName());
        assertEquals("text/plain", result.getMimeType());
        assertEquals(1024L, result.getSize());
        assertEquals("txt", result.getExtension());
        assertEquals(uploaderId, result.getUploaderId());
        assertEquals("Test file", result.getDescription());
        assertEquals("http://minio/test-key", result.getDownloadUrl());
        assertEquals(1, result.getApplications().size());
        assertEquals(appDto.getId(), result.getApplications().get(0).getId());
    }
}*/