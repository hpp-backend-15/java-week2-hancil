package io.hhplus.clean.domain;

import lombok.Getter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
public class LectureResponse {
    private final Long lectureId;
    private final String title;
    private final String lecturer;

    public LectureResponse(Long lectureId, String title, String lecturer) {
        this.lectureId = lectureId;
        this.title = title;
        this.lecturer = lecturer;
    }


}
