package br.zernis.resource;

import br.zernis.dto.product.ProductResponseDTO;
import br.zernis.dto.product.UpsertProductDTO;
import br.zernis.entity.Product;
import br.zernis.service.ProductService;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.UUID;

@Path("/api/products")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProductResource {

    @Inject
    ProductService productService;

    @GET
    @Authenticated
    public List<ProductResponseDTO> listAllProducts() {
        return productService.getAll().stream()
                .map(ProductResponseDTO::fromEntity)
                .toList();
    }

    @GET
    @Path("/{id}")
    @Authenticated
    public Response getProductById(@PathParam("id") UUID id) {
        Product product = productService.getById(id);
        return Response.ok(ProductResponseDTO.fromEntity(product)).build();
    }

    @POST
    @RolesAllowed("SELLER")
    public Response createProduct(@Valid UpsertProductDTO dto) {
        Product product = productService.create(dto);
        return Response.status(Response.Status.CREATED)
                .entity(ProductResponseDTO.fromEntity(product))
                .build();
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed("SELLER")
    public Response updateProduct(@PathParam("id") UUID id, @Valid UpsertProductDTO dto) {
        Product product = productService.update(id, dto);
        return Response.ok(ProductResponseDTO.fromEntity(product)).build();
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed("SELLER")
    public Response deleteProduct(@PathParam("id") UUID id) {
        productService.delete(id);
        return Response.noContent().build();
    }

}
