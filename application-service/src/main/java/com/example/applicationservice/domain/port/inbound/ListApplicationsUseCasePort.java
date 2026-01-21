package com.example.applicationservice.domain.port.inbound;

import com.example.applicationservice.domain.model.entity.Application;

import java.util.List;

/**
 * Получить страницу (список) заявок (простая версия).
 */
public interface ListApplicationsUseCasePort {
    List<Application> listApplications(int page, int size);
}
