package io.hhplus.clean.domain.entity;

import jakarta.persistence.*;


@Entity
@Table(name = "applicants")
public class Applicant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "applicant_id")
    private Long applicantId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecture_id")
    private Lecture lecture;

    // Default constructor for JPA
    protected Applicant() {}

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

    public Lecture getLecture() {
        return lecture;
    }

    public void setLecture(Lecture lecture) {
        this.lecture = lecture;
    }
}
