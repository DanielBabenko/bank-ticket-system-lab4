package com.example.productservice.application.usecase;

import com.example.productservice.application.exception.NotFoundException;
import com.example.productservice.domain.model.entity.Product;
import com.example.productservice.domain.port.outbound.ProductRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GetProductUseCaseTest {

    private ProductRepositoryPort productRepository;
    private GetProductUseCase useCase;

    @BeforeEach
    void setUp() {
        productRepository = mock(ProductRepositoryPort.class);
        useCase = new GetProductUseCase(productRepository);
    }

    @Test
    void getById_productExists_returnsProduct() {
        UUID productId = UUID.randomUUID();
        Product product = Product.createNew(productId, "Name", "Desc");

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        Product result = useCase.getById(productId);

        assertEquals(product, result);
    }

    @Test
    void getById_productNotFound_throwsNotFoundException() {
        UUID productId = UUID.randomUUID();

        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class, () ->
                useCase.getById(productId));

        assertTrue(ex.getMessage().contains("Product not found"));
    }
}
