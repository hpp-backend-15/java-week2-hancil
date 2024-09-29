package io.hhplus.clean;

import io.hhplus.clean.domain.entity.Applicant;
import io.hhplus.clean.domain.entity.Lecture;
import io.hhplus.clean.domain.LectureResponse;
import io.hhplus.clean.repository.ApplicantRepository;
import io.hhplus.clean.repository.LectureRepository;
import io.hhplus.clean.service.LectureService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class LectureSignupServiceTest {


    //- ** 특강 신청 **API**
    //- 특정 userId 로 선착순으로 제공되는 특강을 신청하는 API 를 작성합니다.
    //- 동일한 신청자는 동일한 강의에 대해서 한 번의 수강 신청만 성공할 수 있습니다.
    //- 특강은 선착순 30명만 신청 가능합니다.
    //- 이미 신청자가 30명이 초과되면 이후 신청자는 요청을 실패합니다.

    @Mock
    private LectureRepository lectureRepository;

    @Mock
    private ApplicantRepository applicantRepository;

    @InjectMocks
    private LectureService lectureService;

    private Lecture lecture1;
    private Lecture lecture2;
    private Lecture lecture3;
    private Lecture lecture4;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        //Moick된 LectureRepository의 동작 설정
        this.lecture1 = new Lecture(1L, "TDD", LocalDate.of(2024,9,21), "Huh");
        this.lecture2 = new Lecture(2L, "CleanArchitecture", LocalDate.of(2024, 9, 28), "Huh");
        this.lecture3 = new Lecture(3L, "Server1", LocalDate.now().plusWeeks(1), "Anonymous1");
        this.lecture4 = new Lecture(4L, "Server2", LocalDate.now().plusWeeks(2), "Anonymous2");
        when(lectureRepository.findById(1L)).thenReturn(Optional.of(lecture1));
        when(lectureRepository.findById(2L)).thenReturn(Optional.of(lecture2));
        when(lectureRepository.findById(3L)).thenReturn(Optional.of(lecture3));
        when(lectureRepository.findById(4L)).thenReturn(Optional.of(lecture4));

        when(applicantRepository.existsByEmailAndLecture(anyString(), eq(lecture3))).thenReturn(false)
                .thenReturn(true); // 두 번째 신청할 때는 이미 신청자로 간주

        // lectureRepository의 findAll() 메서드가 호출될 때 강의 목록을 반환하도록 설정
        when(lectureRepository.findAll()).thenReturn(Arrays.asList(lecture1, lecture2, lecture3, lecture4));

    }

    /**
     * 특강 신청 API
     * - 동일한 신청자는 동일한 강의에 대해서 한 번의 수강 신청만 성공할 수 있습니다.
     * - 이미 신청자가 30명이 초과되면 이후 신청자는 요청을 실패합니다.
     * - 특강은 선착순 30명만 신청 가능합니다. (동시성 제어)
     */


    @Test
    @Transactional
    void 내가_특강을_신청하면_성공한다() {

        //given
        Applicant applicant = new Applicant(1L, "정한슬", "beta1992@hanmail.net");

        //when
        //특강 신청
        assertDoesNotThrow(() -> lectureService.applyForLecture(1L, applicant));

        //then
        verify(lectureRepository, times(1)).findById(1L);
        verify(applicantRepository, times(1)).existsByEmailAndLecture("beta1992@hanmail.net", lectureRepository.findById(1L).get());
        verify(applicantRepository, times(1)).save(any(Applicant.class));
        verify(lectureRepository, times(1)).save(any(Lecture.class));


        Lecture lecture = lectureRepository.findById(1L).get();
        assertThat(lecture.getApplicants().size()).isEqualTo(1);
        assertThat(lecture.getApplicants().get(0).getName()).isEqualTo("정한슬");
    }

    @Test
    void 동일_신청자가_두_번_신청하면_신청을_막아야한다() {

        //given
        Applicant applicant = new Applicant(1L, "정한슬", "beta1992@hanmail.net");

        //when
        //특강 신청
        assertDoesNotThrow(() -> lectureService.applyForLecture(3L, applicant));

        //두 번째 신청 시 예외 발생
        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                lectureService.applyForLecture(3L, applicant));

        //then
        assertThat(exception.getMessage()).isEqualTo("이미 신청한 사람입니다.");

        // 추가 검증
        verify(lectureRepository, times(2)).findById(3L); // 두 번 호출되어야 함
        verify(applicantRepository, times(2)).existsByEmailAndLecture(anyString(), any(Lecture.class));
        verify(applicantRepository, times(1)).save(any(Applicant.class)); // 첫 번째 신청만 저장되어야 함
        verify(lectureRepository, times(1)).save(any(Lecture.class)); // 첫 번째 신청만 저장


    }

    @Test
    void 정원을_초과하면_신청에_실패한다() {

        //given
        for(long i=1; i<=30; i++){
            Applicant applicant = new Applicant(i, "applicant"+i, "a"+i+"@hanmail.net");

            // mock으로 각각의 이메일에 대해 existsByEmailAndLecture가 false를 반환하도록 설정
            when(applicantRepository.existsByEmailAndLecture("a" + i + "@hanmail.net", lecture3)).thenReturn(false);
            lectureService.applyForLecture(3L, applicant);
        }

        //when
        //31번째 신청 시 정원 초과 예외 발생
        Applicant over = new Applicant(31L, "정원초과자", "over@hanmail.net");
        when(applicantRepository.existsByEmailAndLecture("over@hanmail.net", lecture3)).thenReturn(false);
        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                lectureService.applyForLecture(3L, over));

        //then
        assertThat(exception.getMessage()).isEqualTo("정원이 초과되었습니다.");

        // 추가 검증
        verify(lectureRepository, times(31)).findById(3L); // 강의 조회가 31 번 일어남
        verify(applicantRepository, times(30)).save(any(Applicant.class)); // 30명의 신청자만 저장
        verify(lectureRepository, times(30)).save(any(Lecture.class)); // 30명만 강의에 저장됨

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

            // mock으로 각각의 이메일에 대해 existsByEmailAndLecture가 false를 반환하도록 설정
            when(applicantRepository.existsByEmailAndLecture("a" + i + "@hanmail.net", lecture3)).thenReturn(false);

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
        applicant.setLecture(lecture3);
        when(applicantRepository.findByApplicantId(1L)).thenReturn(List.of(applicant));

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
