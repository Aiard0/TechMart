package br.zernis.dto.order;

import br.zernis.dto.orderitem.OrderItemRequestDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CreateOrderDTO(
        @NotEmpty @Valid List<OrderItemRequestDTO> items
) {
}
