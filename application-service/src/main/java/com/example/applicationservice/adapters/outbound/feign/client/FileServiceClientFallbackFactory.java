package com.example.applicationservice.adapters.outbound.feign.client;

import com.example.applicationservice.application.exception.ServiceUnavailableException;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class FileServiceClientFallbackFactory implements FallbackFactory<FileServiceClient> {

    @Override
    public FileServiceClient create(Throwable cause) {
        return new FileServiceClient() {
            @Override
            public List<UUID> checkFilesExist(List<UUID> fileIds) {
                // В случае недоступности file-service, логируем и выбрасываем исключение
                throw new ServiceUnavailableException("File service is unavailable now");
            }
        };
    }
}
