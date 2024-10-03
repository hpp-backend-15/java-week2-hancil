package io.hhplus.clean.application.usecase;

import io.hhplus.clean.application.exception.ResourceAlreadyExistsException;
import io.hhplus.clean.application.exception.ResourceNotFoundException;
import io.hhplus.clean.domain.dto.ApplicantDTO;
import io.hhplus.clean.domain.dto.LectureResponseDTO;
import io.hhplus.clean.domain.entity.Applicant;
import io.hhplus.clean.domain.entity.Lecture;
import io.hhplus.clean.domain.repository.ApplicantRepository;
import io.hhplus.clean.domain.repository.LectureRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LectureUseCase {

    private final LectureRepository lectureRepository;
    private final ApplicantRepository applicantRepository;

    @Transactional
    public void applyForLecture(Long lectureId, ApplicantDTO applicant) {

//        Lecture lecture = lectureRepository.findById(lectureId).orElseThrow(() ->
//                new IllegalArgumentException("해당 특강은 존재하지 않습니다."));

        Lecture lecture = lectureRepository.findByIdWithLock(lectureId)
                .orElseThrow(() ->new ResourceNotFoundException("해당 특강은 존재하지 않습니다."));

        boolean alreadyApplied = applicantRepository.existsByEmailAndLecture(applicant.getEmail(), lecture);

        if (alreadyApplied) {
            throw new ResourceAlreadyExistsException("이미 신청한 사람입니다.");
        }

        Applicant newApplicant = new Applicant(null, applicant.getName(), applicant.getEmail(), LocalDateTime.now());
        newApplicant.setLecture(lecture);
        lecture.addApplicant(newApplicant);

        applicantRepository.save(newApplicant); // 신청자를 저장
    }

    public List<LectureResponseDTO> getAvailableLecturesByDate(LocalDate date) {
        return lectureRepository.findAll().stream()
                .filter(lecture -> lecture.getDate().isAfter(date) && lecture.canApply())
                .map(lecture -> new LectureResponseDTO(
                        lecture.getLectureId(),
                        lecture.getTitle(),
                        lecture.getLecturer(),
                        lecture.getDate(),
                        lecture.getApplicants().size(),  // 신청자 수
                        lecture.canApply()))
                .collect(Collectors.toList());

    }

    public List<LectureResponseDTO> getLecturesByUserId(String emailId){

        List<Applicant> applicants = applicantRepository.findByEmail(emailId); // 사용자 ID로 신청자 조회

        return applicants.stream()
                .map(applicant -> new LectureResponseDTO(
                        applicant.getLecture().getLectureId(),
                        applicant.getLecture().getTitle(),
                        applicant.getLecture().getLecturer()))
                .collect(Collectors.toList());
    }

}
