package com.example.userservice.application.usecases;

import com.example.userservice.application.dto.UserDto;
import com.example.userservice.domain.exception.BadRequestException;
import com.example.userservice.domain.exception.NotFoundException;
import com.example.userservice.domain.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class FindUsersUseCase {
    private static final Logger log = LoggerFactory.getLogger(FindUsersUseCase.class);

    private final UserRepository userRepository;
    private final ToUserDto toUserDto;

    public FindUsersUseCase(UserRepository userRepository, ToUserDto toUserDto) {
        this.userRepository = userRepository;
        this.toUserDto = toUserDto;
    }

    public Flux<UserDto> findAll(int page, int size) {
        if (size > 50) {
            return Flux.error(new BadRequestException("Page size cannot exceed 50"));
        }

        return userRepository.findAll()
                .skip((long) page * size)
                .take(size)
                .map(toUserDto::toDto);
    }

    public Mono<UserDto> findById(UUID id) {
        return userRepository.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException("User not found: " + id)))
                .map(toUserDto::toDto);
    }
}
