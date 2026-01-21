package com.example.applicationservice.adapters.outbound.feign;

import com.example.applicationservice.domain.port.outbound.FileServicePort;
import com.example.applicationservice.adapters.outbound.feign.client.FileServiceClient;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class FileServiceAdapter implements FileServicePort {

    private final FileServiceClient client;

    public FileServiceAdapter(FileServiceClient client) {
        this.client = client;
    }

    @Override
    public List<UUID> checkFilesExist(List<UUID> fileIds) {
        return client.checkFilesExist(fileIds);
    }
}
