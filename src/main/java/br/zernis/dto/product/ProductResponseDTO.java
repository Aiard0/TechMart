package br.zernis.dto.product;

import br.zernis.entity.Product;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ProductResponseDTO(
        UUID id,
        String name,
        String description,
        BigDecimal price,
        Integer quantity,
        boolean sold,
        Instant createdAt,
        Instant updatedAt
) {
    public static ProductResponseDTO fromEntity(Product product) {
        return new ProductResponseDTO(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getQuantity(),
                product.isSold(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }
}
