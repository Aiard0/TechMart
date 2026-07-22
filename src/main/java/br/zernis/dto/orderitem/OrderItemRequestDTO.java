package br.zernis.dto.orderitem;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record OrderItemRequestDTO(
        @NotNull UUID productId,
        @NotNull @Min(1) Integer quantity
) {}
