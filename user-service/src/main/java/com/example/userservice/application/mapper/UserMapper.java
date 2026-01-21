package com.example.userservice.application.mapper;

import com.example.userservice.application.dto.UserDto;
import com.example.userservice.domain.model.entity.User;
import org.springframework.stereotype.Service;

@Service
public class UserMapper {
    public UserDto toDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }
}
