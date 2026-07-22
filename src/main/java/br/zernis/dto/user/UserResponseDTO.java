package br.zernis.dto.user;

import br.zernis.entity.User;
import br.zernis.entity.UserRole;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.time.Instant;
import java.util.UUID;

@JsonPropertyOrder({"id", "firstName", "lastName", "email", "role", "createdAt"})
public record UserResponseDTO(
        UUID id,
        String firstName,
        String lastName,
        String email,
        UserRole role,
        Instant createdAt
) {
    public static UserResponseDTO fromEntity(User user) {
        return new UserResponseDTO(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getRole(),
                user.getCreatedAt()
        );
    }
}
