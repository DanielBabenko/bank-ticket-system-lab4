package com.example.assignmentservice.adapters.infrastructure.inbound.transaction;

import com.example.assignmentservice.application.dto.UserProductAssignmentDto;
import com.example.assignmentservice.application.usescases.GetAssignmentsUseCase;
import com.example.assignmentservice.domain.ports.AssignmentMapperPort;
import com.example.assignmentservice.domain.ports.GetAssignmentsUseCasePort;
import com.example.assignmentservice.domain.repository.UserProductAssignmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class GetAssignmentsUseCaseTransactionalDecorator implements GetAssignmentsUseCasePort {
    private final GetAssignmentsUseCase delegate;

    public GetAssignmentsUseCaseTransactionalDecorator(UserProductAssignmentRepository repo,
                                                       AssignmentMapperPort mapper) {
        this.delegate = new GetAssignmentsUseCase(repo, mapper);
    }

    @Override
    public List<UserProductAssignmentDto> list(UUID userId, UUID productId) {
        return delegate.list(userId, productId);
    }
}
