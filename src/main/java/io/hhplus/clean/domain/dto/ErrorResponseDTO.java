package io.hhplus.clean.domain.dto;

public record ErrorResponseDTO(
        String code,
        String message
) {
}
