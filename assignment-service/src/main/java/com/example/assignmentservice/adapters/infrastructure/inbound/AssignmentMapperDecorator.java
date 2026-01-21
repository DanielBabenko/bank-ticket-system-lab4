package com.example.assignmentservice.adapters.infrastructure.inbound;

import com.example.assignmentservice.application.dto.UserProductAssignmentDto;
import com.example.assignmentservice.application.mapper.AssignmentMapper;
import com.example.assignmentservice.domain.model.entity.UserProductAssignment;
import com.example.assignmentservice.domain.ports.AssignmentMapperPort;
import org.springframework.stereotype.Service;

@Service
public class AssignmentMapperDecorator implements AssignmentMapperPort {
    private final AssignmentMapper delegate = new AssignmentMapper();

    @Override
    public UserProductAssignmentDto toDto(UserProductAssignment assignment) {
        return delegate.toDto(assignment);
    }
}
