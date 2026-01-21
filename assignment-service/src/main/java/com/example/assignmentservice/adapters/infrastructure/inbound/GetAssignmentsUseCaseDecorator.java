package com.example.assignmentservice.adapters.infrastructure.inbound;

import com.example.assignmentservice.application.dto.UserProductAssignmentDto;
import com.example.assignmentservice.application.usescases.GetAssignmentsUseCase;
import com.example.assignmentservice.domain.ports.GetAssignmentsUseCasePort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class GetAssignmentsUseCaseDecorator implements GetAssignmentsUseCasePort {
    private final GetAssignmentsUseCase delegate;

    public GetAssignmentsUseCaseDecorator(GetAssignmentsUseCase delegate) {
        this.delegate = delegate;
    }

    @Override
    public List<UserProductAssignmentDto> list(UUID userId, UUID productId) {
        return delegate.list(userId, productId);
    }
}
