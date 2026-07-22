package br.zernis.dto.product;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductSalesDTO(
        UUID productId,
        String productName,
        int soldQuantity,
        BigDecimal totalRevenue,
        String message
) {}
