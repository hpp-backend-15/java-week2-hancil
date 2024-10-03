package io.hhplus.clean.domain.dto;

import lombok.Getter;

@Getter
public class ApplicantDTO {
    private String name;
    private String email;

    public ApplicantDTO(String name, String email) {
        this.name = name;
        this.email = email;
    }

}
