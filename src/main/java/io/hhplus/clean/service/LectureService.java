package io.hhplus.clean.service;

import io.hhplus.clean.domain.Applicant;
import io.hhplus.clean.domain.Lecture;
import io.hhplus.clean.domain.LectureResponse;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LectureService {

    private final Map<Long, Lecture> lectureMap;

    public LectureService() {
        this.lectureMap = new HashMap<>();
    }

    public void applyForLecture(Long lectureId, Applicant applicant) {
        Lecture lecture = lectureMap.get(lectureId);

        if (lecture == null) {
            throw new IllegalArgumentException("해당 특강은 존재하지 않습니다.");
        }

        for (Applicant a : lecture.getApplicants()) {
            if (a.getEmail().equals(applicant.getEmail())) {
                throw new IllegalStateException("이미 신청한 사람입니다.");
            }
        }

        lecture.addApplicant(applicant);
    }

    public void addLecture(Lecture lecture) {
        lectureMap.put(lecture.getLectureId(), lecture);
    }

    public List<Lecture> getAvailableLecturesByDate(LocalDate date) {
        return lectureMap.values().stream()
                .filter(lecture -> lecture.getDate().isAfter(date) && lecture.canApply())
                .collect(Collectors.toList());
    }

    public List<LectureResponse> getLecturesByUserId(Long userId){
        return lectureMap.values().stream()
                .filter(lecture -> lecture.getApplicants().stream()
                        .anyMatch(applicant -> applicant.getApplicantId().equals(userId)))
                .map(lecture -> new LectureResponse(lecture.getLectureId(), lecture.getTitle(), lecture.getLecturer()))
                .collect(Collectors.toList());
    }

    public Map<Long, Lecture> getLectureMap() {
        return lectureMap;
    }
}
