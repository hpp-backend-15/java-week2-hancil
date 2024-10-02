package io.hhplus.clean.domain.dto;

import lombok.Getter;

@Getter
public class LectureResponseDTO {
    private final Long lectureId;
    private final String title;
    private final String lecturer;

    public LectureResponseDTO(Long lectureId, String title, String lecturer) {
        this.lectureId = lectureId;
        this.title = title;
        this.lecturer = lecturer;
    }


}
