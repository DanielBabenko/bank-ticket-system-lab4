package com.example.fileservice.domain.port.inbound;

import com.example.fileservice.domain.model.File;

import java.util.List;

public interface ListFilesUseCasePort {
    List<File> listAll(int page, int size);
}
