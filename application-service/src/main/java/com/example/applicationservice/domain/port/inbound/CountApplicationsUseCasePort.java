package com.example.applicationservice.domain.port.inbound;

/**
 * Счётчик заявок (удобный вспомогательный use-case).
 */
public interface CountApplicationsUseCasePort {
    long count();
}
