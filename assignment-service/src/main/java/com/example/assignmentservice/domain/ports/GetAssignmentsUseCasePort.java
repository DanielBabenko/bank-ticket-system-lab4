package com.example.assignmentservice.domain.ports;

import com.example.assignmentservice.application.dto.UserProductAssignmentDto;

import java.util.List;
import java.util.UUID;

public interface GetAssignmentsUseCasePort {
    List<UserProductAssignmentDto> list(UUID userId, UUID productId);
}
