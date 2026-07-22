package br.zernis.dto.orderitem;

import br.zernis.entity.OrderItem;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderItemResponseDTO(
        UUID id,
        UUID productId,
        String productName,
        int quantity,
        BigDecimal unitPrice
) {
    public static OrderItemResponseDTO fromEntity(OrderItem p) {
        return new OrderItemResponseDTO(
                p.getId(),
                p.getProduct().getId(),
                p.getProduct().getName(),
                p.getQuantity(),
                p.getUnitPrice()
        );
    }
}
