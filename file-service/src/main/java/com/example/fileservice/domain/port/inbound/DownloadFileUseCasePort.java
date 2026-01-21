package com.example.fileservice.domain.port.inbound;

import com.example.fileservice.domain.model.File;
import java.io.InputStream;
import java.util.Optional;
import java.util.UUID;

public interface DownloadFileUseCasePort {
    DownloadResult downloadFile(UUID fileId, UUID userId, Object jwt);

    class DownloadResult {
        public final InputStream inputStream;
        public final String filename;
        public final String contentType;
        public final Long size;

        public DownloadResult(InputStream inputStream, String filename, String contentType, Long size) {
            this.inputStream = inputStream;
            this.filename = filename;
            this.contentType = contentType;
            this.size = size;
        }
    }
}
