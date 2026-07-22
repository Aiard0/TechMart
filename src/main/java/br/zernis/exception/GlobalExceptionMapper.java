package br.zernis.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

import java.time.Instant;
import java.util.stream.Collectors;

@Provider
public class GlobalExceptionMapper {

    @ServerExceptionMapper
    public Response handleBusiness(BusinessException e) {
        return build(e.getStatusCode(), e.getMessage());
    }

    @ServerExceptionMapper
    public Response handleConstraintViolation(ConstraintViolationException e) {
        String message = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("; "));
        return build(400, message);
    }

    private Response build(int statusCode, String message) {
        return Response.status(statusCode)
                .entity(new ErrorResponse(statusCode, message, Instant.now()))
                .build();
    }
}
