package io.hhplus.clean.application.service;

import io.hhplus.clean.domain.entity.Lecture;
import io.hhplus.clean.domain.repository.LectureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LectureService {

    private final LectureRepository lectureRepository;

    public void addLecture(Lecture lecture) {
        lectureRepository.save(lecture);
    }

}
