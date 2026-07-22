package br.zernis.dto.order;

import br.zernis.dto.orderitem.OrderItemResponseDTO;
import br.zernis.entity.Order;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderResponseDTO(
        UUID id,
        UUID buyerId,
        BigDecimal totalPrice,
        Instant createdAt,
        List<OrderItemResponseDTO> items
) {
    public static OrderResponseDTO fromEntity(Order p) {
        return new OrderResponseDTO(
                p.getId(),
                p.getBuyer().getId(),
                p.getTotalPrice(),
                p.getCreatedAt(),
                p.getItems().stream().map(OrderItemResponseDTO::fromEntity).toList()
        );
    }
}
