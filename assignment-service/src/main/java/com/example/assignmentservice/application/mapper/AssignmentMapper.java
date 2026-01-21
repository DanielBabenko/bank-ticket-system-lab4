package com.example.assignmentservice.application.mapper;

import com.example.assignmentservice.application.dto.UserProductAssignmentDto;
import com.example.assignmentservice.domain.model.entity.UserProductAssignment;
import com.example.assignmentservice.domain.ports.AssignmentMapperPort;

public class AssignmentMapper implements AssignmentMapperPort {
    @Override
    public UserProductAssignmentDto toDto(UserProductAssignment assignment) {
        UserProductAssignmentDto dto = new UserProductAssignmentDto();
        dto.setId(assignment.getId());
        dto.setUserId(assignment.getUserId());
        dto.setProductId(assignment.getProductId());
        dto.setRole(assignment.getRoleOnProduct());
        dto.setAssignedAt(assignment.getAssignedAt());
        return dto;
    }
}
