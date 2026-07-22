package br.zernis.resource;

import br.zernis.dto.order.OrderResponseDTO;
import br.zernis.dto.product.ProductSalesDTO;
import br.zernis.entity.Order;
import br.zernis.service.SellerService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.List;
import java.util.UUID;

@Path("/seller")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SellerResource {

    @Inject
    SellerService sellerService;

    @GET
    @Path("/sales")
    @RolesAllowed("SELLER")
    public List<OrderResponseDTO> getSales() {
        return sellerService.listAllOrders().stream()
                .map(OrderResponseDTO::fromEntity)
                .toList();
    }

    @GET
    @Path("/sales/{id}")
    @RolesAllowed("SELLER")
    public ProductSalesDTO getProductHistory(@PathParam("id") UUID id) {
        return sellerService.sellHistory(id);
    }

}
