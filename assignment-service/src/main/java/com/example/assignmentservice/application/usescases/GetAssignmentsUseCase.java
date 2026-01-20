package com.example.assignmentservice.application.usescases;

import com.example.assignmentservice.application.dto.UserProductAssignmentDto;
import com.example.assignmentservice.domain.model.entity.UserProductAssignment;
import com.example.assignmentservice.domain.repository.UserProductAssignmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class GetAssignmentsUseCase {
    private final UserProductAssignmentRepository repo;
    private final ToAssignmentDto toDto;

    public GetAssignmentsUseCase(
            UserProductAssignmentRepository repo, ToAssignmentDto toDto) {
        this.repo = repo;
        this.toDto = toDto;
    }

    @Transactional(readOnly = true)
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
