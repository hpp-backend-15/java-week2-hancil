package io.hhplus.clean.domain.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "lectures")
public class Lecture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lecture_id")
    private Long lectureId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "lecturer", nullable = false)
    private String lecturer;

    @OneToMany(mappedBy = "lecture", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Applicant> applicants = new ArrayList<>();

    @Transient
    private final int MAX_CAPACITY = 30;

    // Default constructor for JPA
    protected Lecture() {}

    public Lecture(Long lectureId, String title, LocalDate date, String lecturer) {
        this.lectureId = lectureId;
        this.title = title;
        this.date = date;
        this.lecturer = lecturer;
    }

    public boolean canApply() {
        return applicants.size() < MAX_CAPACITY;
    }

    public void addApplicant(Applicant applicant) {
        if (canApply()) {
            applicants.add(applicant);
            applicant.setLecture(this); // Set bidirectional relationship
        } else {
            throw new IllegalStateException("정원이 초과되었습니다.");
        }
    }

    public Long getLectureId() {
        return lectureId;
    }

    public String getTitle() {
        return title;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getLecturer() {
        return lecturer;
    }

    public List<Applicant> getApplicants() {
        return applicants;
    }
}
