package com.example.userservice.domain.exception;

public class ConflictException extends RuntimeException {
    public ConflictException(String message) { super(message); }
}
