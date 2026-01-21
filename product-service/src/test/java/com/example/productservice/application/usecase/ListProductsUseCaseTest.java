package com.example.productservice.application.usecase;

import com.example.productservice.application.exception.BadRequestException;
import com.example.productservice.domain.model.entity.Product;
import com.example.productservice.domain.port.outbound.ProductRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ListProductsUseCaseTest {

    private ProductRepositoryPort productRepository;
    private ListProductsUseCase useCase;

    @BeforeEach
    void setUp() {
        productRepository = mock(ProductRepositoryPort.class);
        useCase = new ListProductsUseCase(productRepository);
    }

    @Test
    void list_sizeZero_throwsBadRequest() {
        assertThrows(BadRequestException.class, () -> useCase.list(0, 0));
    }

    @Test
    void list_sizeNegative_throwsBadRequest() {
        assertThrows(BadRequestException.class, () -> useCase.list(0, -5));
    }

    @Test
    void list_success_returnsProducts() {
        Product p1 = Product.createNew(UUID.randomUUID(), "P1", "D1");
        Product p2 = Product.createNew(UUID.randomUUID(), "P2", "D2");

        when(productRepository.findAll(1, 2)).thenReturn(List.of(p1, p2));

        List<Product> result = useCase.list(1, 2);

        assertEquals(2, result.size());
        assertTrue(result.contains(p1));
        assertTrue(result.contains(p2));

        verify(productRepository).findAll(1, 2);
    }
}
