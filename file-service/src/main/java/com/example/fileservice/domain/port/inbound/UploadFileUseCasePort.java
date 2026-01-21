package com.example.fileservice.domain.port.inbound;

import com.example.fileservice.application.command.UploadFileCommand;
import com.example.fileservice.domain.model.File;

public interface UploadFileUseCasePort {
    File uploadFile(UploadFileCommand command);
}
