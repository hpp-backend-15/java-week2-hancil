package io.hhplus.clean.domain;

public class Applicant {
    private Long applicantId;
    private String name;
    private String email;


    public Applicant(Long applicantId, String name, String email) {
        this.applicantId = applicantId;
        this.name = name;
        this.email = email;
    }

    public Long getApplicantId() {
        return applicantId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }
}
