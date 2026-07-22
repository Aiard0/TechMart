package br.zernis.resource;

import br.zernis.dto.auth.LoginDTO;
import br.zernis.dto.auth.LoginResponseDTO;
import br.zernis.dto.user.CreateUserDTO;
import br.zernis.dto.user.UserResponseDTO;
import br.zernis.service.AuthService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

    @Inject
    AuthService authService;

    @POST
    @Path("/login")
    public LoginResponseDTO login(@Valid LoginDTO dto) {
        return authService.login(dto);
    }

    @POST
    @Path("/register")
    public UserResponseDTO register(@Valid CreateUserDTO dto) {
        return authService.register(dto);
    }

}
