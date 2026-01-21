package com.example.productservice.application.usecase;

import com.example.productservice.domain.port.outbound.ProductRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductExistsUseCaseTest {

    private ProductRepositoryPort productRepository;
    private ProductExistsUseCase useCase;

    @BeforeEach
    void setUp() {
        productRepository = mock(ProductRepositoryPort.class);
        useCase = new ProductExistsUseCase(productRepository);
    }

    @Test
    void existsById_productExists_returnsTrue() {
        UUID productId = UUID.randomUUID();
        when(productRepository.existsById(productId)).thenReturn(true);

        boolean result = useCase.existsById(productId);

        assertTrue(result);
        verify(productRepository).existsById(productId);
    }

    @Test
    void existsById_productDoesNotExist_returnsFalse() {
        UUID productId = UUID.randomUUID();
        when(productRepository.existsById(productId)).thenReturn(false);

        boolean result = useCase.existsById(productId);

        assertFalse(result);
        verify(productRepository).existsById(productId);
    }
}
