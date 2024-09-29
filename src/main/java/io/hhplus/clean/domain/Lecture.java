package io.hhplus.clean.domain;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Lecture {
    private Long lectureId;
    private String title;
    private LocalDate date;
    private String lecturer;

    private List<Applicant> applicants = new ArrayList<>();

    // 특강의 정원을 고정값으로 설정
    private final int MAX_CAPACITY = 30;

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
