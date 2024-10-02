package io.hhplus.clean.service;

import io.hhplus.clean.domain.dto.ApplicantDTO;
import io.hhplus.clean.domain.dto.LectureResponseDTO;
import io.hhplus.clean.domain.entity.Applicant;
import io.hhplus.clean.domain.entity.Lecture;
import io.hhplus.clean.repository.ApplicantRepository;
import io.hhplus.clean.repository.LectureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LectureService {

    private final LectureRepository lectureRepository;
    private final ApplicantRepository applicantRepository;

    public void applyForLecture(Long lectureId, ApplicantDTO applicant) {

        Lecture lecture = lectureRepository.findById(lectureId).orElseThrow(() ->
                new IllegalArgumentException("해당 특강은 존재하지 않습니다."));

        boolean alreadyApplied = applicantRepository.existsByEmailAndLecture(applicant.getEmail(), lecture);

        if (alreadyApplied) {
            throw new IllegalStateException("이미 신청한 사람입니다.");
        }

        Applicant newApplicant = new Applicant(null, applicant.getName(), applicant.getEmail());
        newApplicant.setLecture(lecture);
        lecture.addApplicant(newApplicant);

        applicantRepository.save(newApplicant); // 신청자를 저장
//        lectureRepository.save(lecture); //특강도 저장
    }

    public List<Lecture> getAvailableLecturesByDate(LocalDate date) {
        return lectureRepository.findAll().stream()
                .filter(lecture -> lecture.getDate().isAfter(date) && lecture.canApply())
                .collect(Collectors.toList());
    }

    public List<LectureResponseDTO> getLecturesByUserId(String emailId){

        List<Applicant> applicants = applicantRepository.findByEmail(emailId); // 사용자 ID로 신청자 조회

        return applicants.stream()
                .map(applicant -> new LectureResponseDTO(applicant.getLecture().getLectureId(),
                        applicant.getLecture().getTitle(),
                        applicant.getLecture().getLecturer()))
                .collect(Collectors.toList());
    }

    public void addLecture(Lecture lecture) {
        lectureRepository.save(lecture);
    }

}
