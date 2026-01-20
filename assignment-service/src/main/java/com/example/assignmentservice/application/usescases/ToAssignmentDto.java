package com.example.assignmentservice.application.usescases;

import com.example.assignmentservice.application.dto.UserProductAssignmentDto;
import com.example.assignmentservice.domain.model.entity.UserProductAssignment;
import org.springframework.stereotype.Service;

@Service
public class ToAssignmentDto {
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
