package io.hhplus.clean.presentation;


import io.hhplus.clean.application.service.LectureUseCase;
import io.hhplus.clean.domain.dto.ApplicantDTO;
import io.hhplus.clean.domain.dto.LectureResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/lecture")
public class LectureController {

    private final LectureUseCase lectureUseCase;

    @PostMapping("{lectureId}/signup")
    public ResponseEntity<String> signUp(@PathVariable long lectureId,
                                         @RequestBody ApplicantDTO applicantDTO) {
        lectureUseCase.applyForLecture(lectureId, applicantDTO);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/available")
    public ResponseEntity<List<LectureResponseDTO>> getAvailableLecturesByDate(@RequestParam("date")
                                                                    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                                                                    LocalDate date) {
        List<LectureResponseDTO> availableLecturesByDate = lectureUseCase.getAvailableLecturesByDate(date);
        return ResponseEntity.status(HttpStatus.OK).body(availableLecturesByDate);
    }

    @GetMapping("/completed")
    public ResponseEntity<List<LectureResponseDTO>> getCompletedLectures(@RequestParam("email") String email) {

        List<LectureResponseDTO> lectures = lectureUseCase.getLecturesByUserId(email);

        // 특강 목록이 비어있으면 404 응답
        if (lectures.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        // 특강 목록이 있으면 200 응답과 함께 목록 반환
        return ResponseEntity.status(HttpStatus.OK).body(lectures);
    }


}
