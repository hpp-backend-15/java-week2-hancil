package io.hhplus.clean.integration;

import io.hhplus.clean.domain.dto.ApplicantDTO;
import io.hhplus.clean.domain.entity.Lecture;
import io.hhplus.clean.domain.repository.LectureRepository;
import io.hhplus.clean.application.usecase.LectureUseCase;
import io.hhplus.clean.application.service.LectureService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class LectureServiceConcurrencyTest {

    @Autowired
    private LectureService lectureService;

    @Autowired
    private LectureRepository lectureRepository;

    @Autowired
    private LectureUseCase lectureUseCase;


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
    }

    //STEP3
    @Test
    void 동시성_테스트_30명만_성공_나머지는_실패() throws InterruptedException {
        // given
        Long lectureId = lecture3.getLectureId(); // 미리 생성된 특강 ID
        int totalApplicants = 100; // 100명의 신청자
        final int  MAX_CAPACITY_30 = 30;

        // ExecutorService 설정
        ExecutorService executorService = Executors.newFixedThreadPool(totalApplicants); // 100개의 스레드풀
        CountDownLatch latch = new CountDownLatch(totalApplicants); // 100명의 동시 진행을 제어하는 latch

        // when
        for (int i = 0; i < totalApplicants; i++) {
            int I = i;
            executorService.submit(() -> {
                try {
                    ApplicantDTO applicant = new ApplicantDTO("applicant" + I, "a" + I + "@hanmail.net");
                    lectureUseCase.applyForLecture(lectureId, applicant);
                } catch (Exception e) {
                    // 예외 처리 (30명을 넘은 경우 예외가 발생할 것)
                    System.out.println("Exception: " + e.getMessage());
                } finally {
                    latch.countDown(); // latch 감소
                }
            });
        }

        latch.await(); // 모든 스레드가 작업을 완료할 때까지 대기

        // then
        // 30명만 성공해야 함
        Lecture targetLecture = lectureRepository.findById(lecture3.getLectureId()).orElseThrow();
        assertThat(targetLecture.getApplicants().size()).isEqualTo(MAX_CAPACITY_30);

        // Executor 종료
        executorService.shutdown();
    }




}

