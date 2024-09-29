package io.hhplus.clean;

import io.hhplus.clean.domain.Applicant;
import io.hhplus.clean.domain.Lecture;
import io.hhplus.clean.domain.LectureResponse;
import io.hhplus.clean.service.LectureService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class LectureSignupServiceTest {


    //- ** 특강 신청 **API**
    //- 특정 userId 로 선착순으로 제공되는 특강을 신청하는 API 를 작성합니다.
    //- 동일한 신청자는 동일한 강의에 대해서 한 번의 수강 신청만 성공할 수 있습니다.
    //- 특강은 선착순 30명만 신청 가능합니다.
    //- 이미 신청자가 30명이 초과되면 이후 신청자는 요청을 실패합니다.

    private LectureService lectureService;

    @BeforeEach
    void setUp() {
        //LectureService 인스턴스 생성
        lectureService = new LectureService();

        //테스트용 특강 추가
        Lecture lecture1 = new Lecture(1L, "TDD", LocalDate.of(2024,9,21), "Huh");
        Lecture lecture2 = new Lecture(2L, "CleanArchitecture", LocalDate.of(2024, 9, 28), "Huh");
        Lecture lecture3 = new Lecture(3L, "Server1", LocalDate.now().plusWeeks(1), "Anonymous1");
        Lecture lecture4 = new Lecture(4L, "Server2", LocalDate.now().plusWeeks(2), "Anonymous2");
        lectureService.addLecture(lecture1);
        lectureService.addLecture(lecture2);
        lectureService.addLecture(lecture3);
        lectureService.addLecture(lecture4);
    }

    /**
     * 특강 신청 API
     * - 동일한 신청자는 동일한 강의에 대해서 한 번의 수강 신청만 성공할 수 있습니다.
     * - 이미 신청자가 30명이 초과되면 이후 신청자는 요청을 실패합니다.
     * - 특강은 선착순 30명만 신청 가능합니다. (동시성 제어)
     */


    @Test
    void 내가_특강을_신청하면_성공한다() {

        //given
        Applicant applicant = new Applicant(1L, "정한슬", "beta1992@hanmail.net");

        //when
        //특강 신청
        assertDoesNotThrow(() -> lectureService.applyForLecture(1L, applicant));

        //then
        Lecture lecture = lectureService.getLectureMap().get(1L);
        assertThat(lecture.getApplicants().size()).isEqualTo(1);
        assertThat(lecture.getApplicants().get(0).getName()).isEqualTo("정한슬");
    }

    @Test
    void 동일_신청자가_두_번_신청하면_신청을_막아야한다() {

        //given
        Applicant applicant = new Applicant(1L, "정한슬", "beta1992@hanmail.net");

        //when
        //특강 신청
        assertDoesNotThrow(() -> lectureService.applyForLecture(1L, applicant));

        //두 번째 신청 시 예외 발생
        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                lectureService.applyForLecture(1L, applicant));


        //then
        assertThat(exception.getMessage()).isEqualTo("이미 신청한 사람입니다.");
    }

    @Test
    void 정원을_초과하면_신청에_실패한다() {

        //given
        for(long i=1; i<=30; i++){
            Applicant applicant = new Applicant(i, "applicant"+i, "a"+i+"@hanmail.net");
            lectureService.applyForLecture(1L, applicant);
        }

        //when
        //31번째 신청 시 정원 초과 예외 발생
        Applicant over = new Applicant(31L, "정원초과자", "over@hanmail.net");
        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                lectureService.applyForLecture(1L, over));

        //then
        assertThat(exception.getMessage()).isEqualTo("정원이 초과되었습니다.");
    }

    @Test
    void 존재하지_않는_강의를_신청할_경우_예외() {
        //given
        Long lectureId = -999L;
        Applicant applicant = new Applicant(1L, "정한슬", "beta1992@hanmail.net");


        //when
        //존재하지 않는 특강을 신청
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                lectureService.applyForLecture(lectureId, applicant));

        //then
        assertThat(exception.getMessage()).isEqualTo("해당 특강은 존재하지 않습니다.");

    }


    /**
     * 특강 선택 API
     * - 날짜별로 현재 신청 가능한 특강 목록을 조회하는 API 를 작성
     * - 특강의 정원은 30명으로 고정이며, 사용자는 각 특강에 신청하기전 목록을 조회해볼 수 있어야함
     */
    @Test
    void 특정_날짜에_신청_가능한_특강이_없을_때() {

        //오늘 날짜에 신청 가능한 특강은 2개 밖에 없어야함
        List<Lecture> availableLecturesByDate = lectureService.getAvailableLecturesByDate(LocalDate.now());
        assertEquals(2, availableLecturesByDate.size());
    }

    @Test
    void 정원_꽉_찬_특강은_조회되지_않아야_함() {

        //given
        for(long i=1; i<=30; i++){
            Applicant applicant = new Applicant(i, "applicant"+i, "a"+i+"@hanmail.net");
            lectureService.applyForLecture(3L, applicant);
        }

        //when
        List<Lecture> availableLecturesByDate = lectureService.getAvailableLecturesByDate(LocalDate.now());

        //then
        //Server1 특강은 정원초과로 신청이 불가능하므로 Server2 특강만 조회 가능
        assertEquals(1, availableLecturesByDate.size());
        assertFalse(availableLecturesByDate.stream().anyMatch(lecture -> lecture.getTitle().equals("Server1")));
        assertThat(availableLecturesByDate.get(0).getTitle()).isEqualTo("Server2");

    }


    /**
     * 특강 신청 완료 목록 조회 API
     * 특정 userId로 신청 완료된 특강 목록을 조회하는 API를 작성합니다.
     * 각 항목은 특강 ID 및 이름, 강연자 정보를 담고 있어야 합니다.
     */


    @Test
    void 수강_신청_완료된_특강_조회() {

        //given
        Applicant applicant = new Applicant(1L, "정한슬", "beta1992@hanmail.net");
        lectureService.applyForLecture(3L, applicant);

        //when
        List<LectureResponse> lectures = lectureService.getLecturesByUserId(1L);

        assertThat(lectures.size()).isEqualTo(1);
        assertThat(lectures.get(0).getTitle()).isEqualTo("Server1");
        assertThat(lectures.get(0).getLecturer()).isEqualTo("Anonymous1");

    }

    @Test
    void 신청된_특강이_없으면() {
        List<LectureResponse> lectures = lectureService.getLecturesByUserId(1L);
        assertThat(lectures).isEmpty();
    }
}
