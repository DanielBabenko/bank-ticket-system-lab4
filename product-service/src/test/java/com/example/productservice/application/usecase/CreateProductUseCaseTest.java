package com.example.productservice.application.usecase;

import com.example.productservice.application.exception.BadRequestException;
import com.example.productservice.application.exception.ConflictException;
import com.example.productservice.domain.model.entity.Product;
import com.example.productservice.domain.port.outbound.ProductRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CreateProductUseCaseTest {

    private ProductRepositoryPort productRepository;
    private CreateProductUseCase useCase;

    @BeforeEach
    void setUp() {
        productRepository = mock(ProductRepositoryPort.class);
        useCase = new CreateProductUseCase(productRepository);
    }

    @Test
    void create_nameNull_throwsBadRequest() {
        assertThrows(BadRequestException.class, () ->
                useCase.create(null, "desc"));
    }

    @Test
    void create_nameEmpty_throwsBadRequest() {
        assertThrows(BadRequestException.class, () ->
                useCase.create("   ", "desc"));
    }

    @Test
    void create_nameAlreadyExists_throwsConflict() {
        String name = "Product1";
        when(productRepository.existsByName(name)).thenReturn(true);

        assertThrows(ConflictException.class, () ->
                useCase.create(name, "desc"));
    }

    @Test
    void create_validProduct_savesAndReturns() {
        String name = "NewProduct";
        String description = "Description";
        when(productRepository.existsByName(name)).thenReturn(false);

        Product savedProduct = Product.createNew(UUID.randomUUID(), name, description);
        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

        Product result = useCase.create(name, description);

        assertNotNull(result);
        assertEquals(name, result.getName());
        assertEquals(description, result.getDescription());
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void create_nameWithSpaces_trimsBeforeSaving() {
        String name = "  ProductX  ";
        String trimmed = "ProductX";
        when(productRepository.existsByName(trimmed)).thenReturn(false);

        Product savedProduct = Product.createNew(UUID.randomUUID(), trimmed, "desc");
        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

        Product result = useCase.create(name, "desc");

        assertEquals(trimmed, result.getName());
        verify(productRepository).save(any(Product.class));
    }
}
