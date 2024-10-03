package io.hhplus.clean.domain.dto;

public record ErrorResponse(
        String code,
        String message
) {
}
