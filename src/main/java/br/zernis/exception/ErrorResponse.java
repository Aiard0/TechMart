package br.zernis.exception;

import java.time.Instant;

public record ErrorResponse(
        int statusCode,
        String message,
        Instant timestamp
) {}
