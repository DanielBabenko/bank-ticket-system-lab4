package com.example.assignmentservice.domain.ports;

import com.example.assignmentservice.application.dto.UserProductAssignmentDto;
import com.example.assignmentservice.domain.model.entity.UserProductAssignment;

public interface AssignmentMapperPort {
    UserProductAssignmentDto toDto(UserProductAssignment assignment);
}
