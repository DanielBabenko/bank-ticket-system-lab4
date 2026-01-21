package com.example.applicationservice.domain.port.outbound;

import java.util.List;
import java.util.UUID;

public interface FileServicePort {
    /**
     * Возвращает список существующих из переданных идентификаторов.
     * При недоступности — бросает исключение или возвращает null (как решите в адаптере).
     */
    List<UUID> checkFilesExist(List<UUID> fileIds);
}
