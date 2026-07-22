package br.zernis.resource;

import br.zernis.dto.order.CreateOrderDTO;
import br.zernis.dto.order.OrderResponseDTO;
import br.zernis.entity.Order;
import br.zernis.service.OrderService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.UUID;

@Path("/orders")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class OrderResource {

    @Inject
    OrderService orderService;

    @POST
    @RolesAllowed("USER")
    public Response createOrder(@Valid CreateOrderDTO dto) {
        Order order = orderService.buy(dto.items());
        return Response.status(Response.Status.CREATED)
                .entity(OrderResponseDTO.fromEntity(order))
                .build();
    }

    @GET
    @RolesAllowed("USER")
    public List<OrderResponseDTO> listOrders() {
        return orderService.listByBuyer().stream()
                .map(OrderResponseDTO::fromEntity)
                .toList();
    }

}
