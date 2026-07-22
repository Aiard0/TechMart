package br.zernis.dto.user;

import br.zernis.entity.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateUserDTO(
        @NotBlank String firstName,
        String lastName,
        @NotBlank @Email String email,
        @NotBlank String password,
        @NotNull UserRole role
) {}
