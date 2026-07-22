package br.zernis.resource;

import br.zernis.dto.user.UserResponseDTO;
import br.zernis.entity.User;
import br.zernis.service.UserService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.UUID;

@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserResource {

    @Inject
    UserService userService;

    @GET
    @RolesAllowed("SELLER")
    public List<UserResponseDTO> listAllUsers() {
        return userService.getAll().stream()
                .map(UserResponseDTO::fromEntity)
                .toList();
    }

    @GET
    @Path("/{id}")
    @RolesAllowed("SELLER")
    public Response getUserById(@PathParam("id") UUID id) {
        User user = userService.getById(id);
        return Response.ok(UserResponseDTO.fromEntity(user)).build();
    }

}
