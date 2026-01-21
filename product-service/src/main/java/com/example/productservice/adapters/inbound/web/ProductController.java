package com.example.productservice.adapters.inbound.web;

import com.example.productservice.application.dto.ProductDto;
import com.example.productservice.application.dto.ProductRequest;
import com.example.productservice.application.mapper.ProductMapper;
import com.example.productservice.application.exception.BadRequestException;
import com.example.productservice.application.exception.UnauthorizedException;
import com.example.productservice.domain.port.inbound.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Tag(name = "Products", description = "API for managing products")
@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);
    private static final int MAX_PAGE_SIZE = 50;

    private final CreateProductUseCasePort createProduct;
    private final ListProductsUseCasePort listProducts;
    private final GetProductUseCasePort getProduct;
    private final UpdateProductUseCasePort updateProduct;
    private final DeleteProductUseCasePort deleteProduct;
    private final ProductExistsUseCasePort productExists;

    public ProductController(
            CreateProductUseCasePort createProduct,
            ListProductsUseCasePort listProducts,
            GetProductUseCasePort getProduct,
            UpdateProductUseCasePort updateProduct,
            DeleteProductUseCasePort deleteProduct,
            ProductExistsUseCasePort productExists
    ) {
        this.createProduct = createProduct;
        this.listProducts = listProducts;
        this.getProduct = getProduct;
        this.updateProduct = updateProduct;
        this.deleteProduct = deleteProduct;
        this.productExists = productExists;
    }

    @Operation(summary = "Create a new product", description = "Registers a new product: name, description")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Product created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request body"),
            @ApiResponse(responseCode = "409", description = "Product name already in use")
    })
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ProductDto> create(@Valid @RequestBody ProductRequest req, UriComponentsBuilder uriBuilder) {
        var created = createProduct.create(req.getName(), req.getDescription());
        ProductDto dto = ProductMapper.toDto(created);

        URI location = uriBuilder.path("/api/v1/products/{id}").buildAndExpand(dto.getId()).toUri();
        logger.info("Product created with ID: {}", dto.getId());
        return ResponseEntity.created(location).body(dto);
    }

    @Operation(summary = "Read all products with pagination", description = "Returns a paginated list of products")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of products"),
            @ApiResponse(responseCode = "400", description = "Page size too large")
    })
    @GetMapping
    public ResponseEntity<List<ProductDto>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletResponse response) {

        if (size > MAX_PAGE_SIZE) {
            throw new BadRequestException("size cannot be greater than " + MAX_PAGE_SIZE);
        }

        List<com.example.productservice.domain.model.entity.Product> products = listProducts.list(page, size);
        List<ProductDto> dtos = products.stream().map(ProductMapper::toDto).collect(Collectors.toList());

        response.setHeader("X-Total-Count", String.valueOf(dtos.size()));

        logger.debug("Returning {} products from page {}", dtos.size(), page);
        return ResponseEntity.ok().body(dtos);
    }

    @Operation(summary = "Read certain product by its ID", description = "Returns data about a single product")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Data about a single product"),
            @ApiResponse(responseCode = "404", description = "Product with this ID is not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> get(@PathVariable UUID id) {
        var product = getProduct.getById(id);
        if (product == null) { // defensive; use-case throws NotFound normally
            logger.debug("Product not found: {}", id);
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(ProductMapper.toDto(product));
    }

    @Operation(summary = "Update the data of a specific product", description = "Update any data of single product")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request body"),
            @ApiResponse(responseCode = "401", description = "Actor is unauthorized"),
            @ApiResponse(responseCode = "403", description = "Insufficient rights"),
            @ApiResponse(responseCode = "404", description = "Product or actor not found"),
            @ApiResponse(responseCode = "409", description = "Product name already in use"),
            @ApiResponse(responseCode = "503", description = "User service is unavailable")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ProductDto> updateProduct(
            @PathVariable("id") UUID id,
            @Valid @RequestBody ProductRequest req,
            @AuthenticationPrincipal Jwt jwt) {

        if (jwt == null) {
            throw new UnauthorizedException("No authentication token");
        }
        String uidStr = jwt.getClaimAsString("uid");
        if (uidStr == null) uidStr = jwt.getSubject();
        UUID actorId = UUID.fromString(uidStr);

        logger.info("Updating product {} by actor {}", id, actorId);
        var updated = updateProduct.update(id, req.getName(), req.getDescription(), actorId, isAdmin(jwt));
        return ResponseEntity.ok(ProductMapper.toDto(updated));
    }

    @Operation(summary = "Delete a specific product", description = "Deletes one specific product from the database")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Product deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Actor is unauthorized"),
            @ApiResponse(responseCode = "403", description = "Insufficient rights"),
            @ApiResponse(responseCode = "404", description = "Product or actor not found"),
            @ApiResponse(responseCode = "409", description = "Error during deletion"),
            @ApiResponse(responseCode = "503", description = "User or application service is unavailable")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable("id") UUID id,
            @AuthenticationPrincipal Jwt jwt) {

        if (jwt == null) {
            throw new UnauthorizedException("No authentication token");
        }
        String uidStr = jwt.getClaimAsString("uid");
        if (uidStr == null) uidStr = jwt.getSubject();
        UUID actorId = UUID.fromString(uidStr);

        logger.info("Deleting product {} by actor {}", id, actorId);
        deleteProduct.delete(id, actorId, isAdmin(jwt));
        return ResponseEntity.noContent().build();
    }

    // Internal endpoint for other services
    @GetMapping("/{id}/exists")
    public ResponseEntity<Boolean> productExists(@PathVariable UUID id) {
        boolean exists = productExists.existsById(id);
        return ResponseEntity.ok(exists);
    }

    private boolean isAdmin(Jwt jwt) {
        if (jwt == null) return false;
        Object roleClaim = jwt.getClaims().get("role");
        return roleClaim != null && "ROLE_ADMIN".equals(roleClaim.toString());
    }
}
