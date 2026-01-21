package com.example.assignmentservice.application.usescases;

import com.example.assignmentservice.application.dto.UserProductAssignmentDto;
import com.example.assignmentservice.domain.model.entity.UserProductAssignment;
import com.example.assignmentservice.domain.ports.AssignmentMapperPort;
import com.example.assignmentservice.domain.ports.GetAssignmentsUseCasePort;
import com.example.assignmentservice.domain.repository.UserProductAssignmentRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class GetAssignmentsUseCase implements GetAssignmentsUseCasePort {
    private final UserProductAssignmentRepository repo;
    private final AssignmentMapperPort toDto;

    public GetAssignmentsUseCase(
            UserProductAssignmentRepository repo, AssignmentMapperPort toDto) {
        this.repo = repo;
        this.toDto = toDto;
    }

    @Override
    public List<UserProductAssignmentDto> list(UUID userId, UUID productId) {
        List<UserProductAssignment> assignments;

        if (userId != null) {
            assignments = repo.findByUserId(userId);
        } else if (productId != null) {
            assignments = repo.findByProductId(productId);
        } else {
            assignments = repo.findAll();
        }

        return assignments.stream()
                .map(toDto::toDto)
                .collect(Collectors.toList());
    }
}
