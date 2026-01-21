package com.example.productservice.application.usecase;

import com.example.productservice.application.exception.*;
import com.example.productservice.domain.model.entity.Product;
import com.example.productservice.domain.port.outbound.AssignmentServicePort;
import com.example.productservice.domain.port.outbound.ProductRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UpdateProductUseCaseTest {

    private ProductRepositoryPort productRepository;
    private AssignmentServicePort assignmentService;
    private UpdateProductUseCase useCase;

    @BeforeEach
    void setUp() {
        productRepository = mock(ProductRepositoryPort.class);
        assignmentService = mock(AssignmentServicePort.class);
        useCase = new UpdateProductUseCase(productRepository, assignmentService);
    }

    @Test
    void update_actorIdNull_throwsUnauthorized() {
        assertThrows(UnauthorizedException.class, () ->
                useCase.update(UUID.randomUUID(), "Name", "Desc", null, true));
    }

    @Test
    void update_productNotFound_throwsNotFound() {
        UUID productId = UUID.randomUUID();
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () ->
                useCase.update(productId, "Name", "Desc", UUID.randomUUID(), true));
    }

    @Test
    void update_notAdminNotOwner_throwsForbidden() {
        UUID productId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        Product product = Product.createNew(productId, "OldName", "OldDesc");

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(assignmentService.hasRole(actorId, productId, "PRODUCT_OWNER")).thenReturn(false);

        assertThrows(ForbiddenException.class, () ->
                useCase.update(productId, "NewName", "NewDesc", actorId, false));
    }

    @Test
    void update_assignmentServiceUnavailable_throwsServiceUnavailable() {
        UUID productId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        Product product = Product.createNew(productId, "OldName", "OldDesc");

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(assignmentService.hasRole(actorId, productId, "PRODUCT_OWNER")).thenReturn(null);

        assertThrows(ServiceUnavailableException.class, () ->
                useCase.update(productId, "NewName", "NewDesc", actorId, false));
    }

    @Test
    void update_nameConflict_throwsConflict() {
        UUID productId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        Product product = Product.createNew(productId, "OldName", "OldDesc");

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productRepository.existsByName("NewName")).thenReturn(true);

        assertThrows(ConflictException.class, () ->
                useCase.update(productId, "NewName", "OldDesc", actorId, true));
    }

    @Test
    void update_adminCanUpdateNameAndDescription() {
        UUID productId = UUID.randomUUID();
        Product product = Product.createNew(productId, "OldName", "OldDesc");

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productRepository.existsByName("NewName")).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenAnswer(i -> i.getArgument(0));

        Product updated = useCase.update(productId, "NewName", "NewDesc", UUID.randomUUID(), true);

        assertEquals("NewName", updated.getName());
        assertEquals("NewDesc", updated.getDescription());
        verify(productRepository).save(product);
    }

    @Test
    void update_ownerCanUpdate() {
        UUID productId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        Product product = Product.createNew(productId, "OldName", "OldDesc");

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(assignmentService.hasRole(actorId, productId, "PRODUCT_OWNER")).thenReturn(true);
        when(productRepository.existsByName("NewName")).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenAnswer(i -> i.getArgument(0));

        Product updated = useCase.update(productId, "NewName", "NewDesc", actorId, false);

        assertEquals("NewName", updated.getName());
        assertEquals("NewDesc", updated.getDescription());
        verify(productRepository).save(product);
    }

    @Test
    void update_nameOrDescriptionNull_doesNotChange() {
        UUID productId = UUID.randomUUID();
        Product product = Product.createNew(productId, "OldName", "OldDesc");

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(i -> i.getArgument(0));

        // Only description null
        Product updated1 = useCase.update(productId, "NewName", null, UUID.randomUUID(), true);
        assertEquals("NewName", updated1.getName());
        assertEquals("OldDesc", updated1.getDescription());

        // Only name null
        Product updated2 = useCase.update(productId, null, "NewDesc", UUID.randomUUID(), true);
        assertEquals("NewName", updated2.getName());
        assertEquals("NewDesc", updated2.getDescription());
    }
}
