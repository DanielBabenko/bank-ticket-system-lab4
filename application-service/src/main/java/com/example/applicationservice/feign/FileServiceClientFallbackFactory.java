package com.example.applicationservice.feign;

import com.example.applicationservice.dto.AttachFileRequest;
import com.example.applicationservice.dto.FileMetadataDto;
import com.example.applicationservice.exception.ServiceUnavailableException;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Component
public class FileServiceClientFallbackFactory implements FallbackFactory<FileServiceClient> {

    @Override
    public FileServiceClient create(Throwable cause) {
        return new FileServiceClient() {

            @Override
            public List<FileMetadataDto> getFilesByApplication(UUID applicationId) {
                return Collections.emptyList();
            }

            @Override
            public FileMetadataDto getFileMetadata(UUID fileId) {
                throw new ServiceUnavailableException("File service is unavailable. Cannot get metadata for file: " + fileId);
            }

            @Override
            public void attachFileToApplication(UUID fileId, AttachFileRequest request) {
                throw new ServiceUnavailableException("File service is unavailable. Cannot attach file: " + fileId + " to application");
            }
        };
    }
}