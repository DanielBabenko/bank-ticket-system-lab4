package com.example.userservice.domain.ports.inbound;

import com.example.userservice.application.dto.UserDto;
import com.example.userservice.domain.model.entity.User;

public interface UserMapperPort {
    UserDto toDto(User user);
}
