package br.zernis.dto.auth;

public record LoginResponseDTO(
        String email,
        String token
) {
}
