package io.hhplus.clean.integration;

import io.hhplus.clean.domain.dto.ApplicantDTO;
import io.hhplus.clean.domain.dto.LectureResponseDTO;
import io.hhplus.clean.domain.entity.Applicant;
import io.hhplus.clean.domain.entity.Lecture;
import io.hhplus.clean.repository.ApplicantRepository;
import io.hhplus.clean.repository.LectureRepository;
import io.hhplus.clean.service.LectureService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 다수의 인스턴스 어플리케이션 고려 X
 * 동시성 이슈 고려 X
 */

@SpringBootTest
public class LectureSignupServiceIntegrationTest {

    @Autowired
    private ApplicantRepository applicantRepository;

    @Autowired
    private LectureRepository lectureRepository;


    @Autowired
    private LectureService lectureService;

    private Lecture lecture1;
    private Lecture lecture2;
    private Lecture lecture3;
    private Lecture lecture4;

    @BeforeEach
    void setup() {
        // Lecture 저장
        this.lecture1 = new Lecture(1L, "TDD", LocalDate.of(2024,9,21), "Huh");
        this.lecture2 = new Lecture(2L, "CleanArchitecture", LocalDate.of(2024, 9, 28), "Huh");
        this.lecture3 = new Lecture(3L, "Server1", LocalDate.now().plusWeeks(1), "Anonymous1");
        this.lecture4 = new Lecture(4L, "Server2", LocalDate.now().plusWeeks(2), "Anonymous2");
        lectureService.addLecture(lecture1);
        lectureService.addLecture(lecture2);
        lectureService.addLecture(lecture3);
        lectureService.addLecture(lecture4);


        for(long i=1; i<=30; i++){
            ApplicantDTO applicantDTO = new ApplicantDTO("applicant"+i, "a"+i+"@hanmail.net");
            lectureService.applyForLecture(lecture3.getLectureId(), applicantDTO);
        }

    }


    @Test
    @Transactional // 트랜잭션을 유지하기 위해 추가
    void 단일_신청자_insert_test() {

        ApplicantDTO applicantDTO = new ApplicantDTO("정한슬", "beta1992@hanmail.net");

        boolean alreadyApplied = applicantRepository.existsByEmailAndLecture(applicantDTO.getEmail(), lecture4);
        assertThat(alreadyApplied).isFalse();

        Applicant newApplicant = new Applicant(null, applicantDTO.getName(), applicantDTO.getEmail());
        newApplicant.setLecture(lecture4);
        lecture4.addApplicant(newApplicant);

        Applicant savedApplicant = applicantRepository.save(newApplicant);// 신청자를 저장
        assertThat(savedApplicant.getName()).isEqualTo(applicantDTO.getName());
        assertThat(savedApplicant.getEmail()).isEqualTo(applicantDTO.getEmail());

        assertThat(lecture4.getApplicants().size()).isEqualTo(1);
    }


    @Test
    @Transactional // 트랜잭션을 유지하기 위해 추가
    void 정원을_초과하면_신청에_실패한다() {

        // setUp에 30명의 수강신청자가 존재함.
        // Server1의 수강자는 30명임을 확인
        Lecture testLecture = lectureRepository.findById(lecture3.getLectureId()).orElseThrow(() ->
                new IllegalArgumentException("해당 특강은 존재하지 않습니다."));
        assertThat(testLecture.getApplicants().size()).isEqualTo(30);

        //given
        ApplicantDTO applicantDTO = new ApplicantDTO("정원초과자", "over@hanmail.net");

        //when
        //31번째 신청 시 정원 초과 예외 발생
        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                lectureService.applyForLecture(lecture3.getLectureId(), applicantDTO));
        //then
        assertThat(exception.getMessage()).isEqualTo("정원이 초과되었습니다.");

    }


    @Test
    @Transactional // 트랜잭션을 유지하기 위해 추가
    void 동일_신청자는_동일한_강의에_대해_두_번_신청_하면_실패한다() {

        //given
        ApplicantDTO applicantDTO = new ApplicantDTO("정한슬", "beta1992@hanmail.net");
        lectureService.applyForLecture(lecture4.getLectureId(), applicantDTO);


        //when
        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                lectureService.applyForLecture(lecture4.getLectureId(), applicantDTO));

        //then
        assertThat(exception.getMessage()).isEqualTo("이미 신청한 사람입니다.");

    }

    @Test
    @Transactional
    void 날짜별_현재_신청_가능한_특강_목록() {

        //정원이 꽉 찼고, 특강날이 지난 목록은 조회되지 않아야 하므로
        List<Lecture> availableLecturesByDate = lectureService.getAvailableLecturesByDate(LocalDate.now());
        assertEquals(1, availableLecturesByDate.size());
    }


    @Test
    @Transactional
    void 수강_신청_완료된_특강_조회() {

        //given
        ApplicantDTO applicantDTO = new ApplicantDTO("applicant30", "a30@hanmail.net");

        List<LectureResponseDTO> lectureResponse = lectureService.getLecturesByUserId(applicantDTO.getEmail());

        assertThat(lectureResponse.size()).isEqualTo(1);
        assertThat(lectureResponse.get(0).getTitle()).isEqualTo("Server1");
        assertThat(lectureResponse.get(0).getLecturer()).isEqualTo("Anonymous1");

    }

    @Test
    @Transactional
    void 신청된_특강이_없으면() {
        List<LectureResponseDTO> lectures = lectureService.getLecturesByUserId("beta1992@hanmail.net");
        assertThat(lectures).isEmpty();
    }


}
