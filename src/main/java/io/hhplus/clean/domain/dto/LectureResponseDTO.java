package io.hhplus.clean.domain.dto;

import lombok.Getter;

import java.time.LocalDate;

@Getter
public class LectureResponseDTO {
    private final Long lectureId;
    private final String title;
    private final String lecturer;
    private LocalDate date;
    private int currentApplicants;  // 현재 신청자 수
    private boolean canApply;       // 신청 가능 여부

    public LectureResponseDTO(Long lectureId, String title, String lecturer, LocalDate date, int currentApplicants, boolean canApply) {
        this.lectureId = lectureId;
        this.title = title;
        this.lecturer = lecturer;
        this.date = date;
        this.currentApplicants = currentApplicants;
        this.canApply = canApply;
    }


    public LectureResponseDTO(Long lectureId, String title, String lecturer) {
        this.lectureId = lectureId;
        this.title = title;
        this.lecturer = lecturer;
    }


}
