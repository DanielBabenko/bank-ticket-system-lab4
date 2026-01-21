package com.example.productservice.application.usecase;

import com.example.productservice.application.exception.*;
import com.example.productservice.domain.model.entity.Product;
import com.example.productservice.domain.port.outbound.AssignmentServicePort;
import com.example.productservice.domain.port.outbound.ProductEventPublisherPort;
import com.example.productservice.domain.port.outbound.ProductRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DeleteProductUseCaseTest {

    private ProductRepositoryPort productRepository;
    private AssignmentServicePort assignmentService;
    private ProductEventPublisherPort eventPublisher;
    private DeleteProductUseCase useCase;

    @BeforeEach
    void setUp() {
        productRepository = mock(ProductRepositoryPort.class);
        assignmentService = mock(AssignmentServicePort.class);
        eventPublisher = mock(ProductEventPublisherPort.class);

        useCase = new DeleteProductUseCase(productRepository, assignmentService, eventPublisher);
    }

    @Test
    void delete_actorIdNull_throwsUnauthorized() {
        assertThrows(UnauthorizedException.class, () ->
                useCase.delete(UUID.randomUUID(), null, true));
    }

    @Test
    void delete_productNotFound_throwsNotFound() {
        UUID productId = UUID.randomUUID();
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () ->
                useCase.delete(productId, UUID.randomUUID(), true));
    }

    @Test
    void delete_notAdminNotOwner_throwsForbidden() {
        UUID productId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        Product product = Product.createNew(productId, "Name", "Desc");

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(assignmentService.hasRole(actorId, productId, "PRODUCT_OWNER")).thenReturn(false);

        assertThrows(ForbiddenException.class, () ->
                useCase.delete(productId, actorId, false));
    }

    @Test
    void delete_assignmentServiceUnavailable_throwsServiceUnavailable() {
        UUID productId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        Product product = Product.createNew(productId, "Name", "Desc");

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(assignmentService.hasRole(actorId, productId, "PRODUCT_OWNER")).thenReturn(null);

        assertThrows(ServiceUnavailableException.class, () ->
                useCase.delete(productId, actorId, false));
    }

    @Test
    void delete_admin_canDelete() {
        UUID productId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        Product product = Product.createNew(productId, "Name", "Desc");

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        assertDoesNotThrow(() ->
                useCase.delete(productId, actorId, true));

        verify(productRepository).delete(product);
        verify(eventPublisher).publishProductDeleted(productId);
    }

    @Test
    void delete_owner_canDelete() {
        UUID productId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        Product product = Product.createNew(productId, "Name", "Desc");

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(assignmentService.hasRole(actorId, productId, "PRODUCT_OWNER")).thenReturn(true);

        assertDoesNotThrow(() ->
                useCase.delete(productId, actorId, false));

        verify(productRepository).delete(product);
        verify(eventPublisher).publishProductDeleted(productId);
    }
}
