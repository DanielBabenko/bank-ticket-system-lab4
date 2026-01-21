package com.example.applicationservice.application.usecase;

import com.example.applicationservice.application.exception.BadRequestException;
import com.example.applicationservice.application.usecase.CreateApplicationUseCase;
import com.example.applicationservice.domain.dto.ApplicationInfo;
import com.example.applicationservice.domain.model.entity.Application;
import com.example.applicationservice.domain.model.entity.ApplicationHistory;
import com.example.applicationservice.domain.model.enums.ApplicationStatus;
import com.example.applicationservice.domain.port.outbound.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.*;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CreateApplicationUseCaseTest {

    @Mock ApplicationRepositoryPort repo;
    @Mock
    ApplicationHistoryRepositoryPort historyRepo;
    @Mock UserServicePort userService;
    @Mock ProductServicePort productService;
    @Mock FileServicePort fileService;
    @Mock EventPublisherPort eventPublisher;
    // if CreateApplicationUseCase depends on other ports (kafka) you can mock them as well

    CreateApplicationUseCase usecase;

    @BeforeEach
    void setUp() {
        usecase = new CreateApplicationUseCase(repo, historyRepo, userService, productService, fileService, eventPublisher);
    }

    @Test
    void create_nullRequest_shouldThrow() {
        UUID actor = UUID.randomUUID();
        assertThrows(BadRequestException.class, () -> usecase.createApplication(null, actor, "ROLE_CLIENT"));
    }

    @Test
    void create_success_whenAdminOrOwner() {
        UUID admin = UUID.randomUUID();
        UUID applicant = UUID.randomUUID();
        UUID product = UUID.randomUUID();

        var req = new com.example.applicationservice.domain.dto.ApplicationCreateCommand();
        req.setApplicantId(applicant);
        req.setProductId(product);
        req.setFiles(List.of());
        req.setTags(List.of("t1"));

        when(userService.userExists(applicant)).thenReturn(true);
        when(productService.productExists(product)).thenReturn(true);

        // simulate repo saving returns domain Application with id
        when(repo.save(any(Application.class))).thenAnswer(inv -> {
            Application a = inv.getArgument(0);
            if (a.getId() == null) a.setId(UUID.randomUUID());
            a.setCreatedAt(Instant.now());
            return a;
        });

        var result = usecase.createApplication(req, admin, "ROLE_ADMIN");
        assertNotNull(result);
        assertEquals(applicant, result.getApplicantId());
        assertEquals(product, result.getProductId());
    }
}
