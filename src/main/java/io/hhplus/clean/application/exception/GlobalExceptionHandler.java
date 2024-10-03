package io.hhplus.clean.application.exception;

import io.hhplus.clean.domain.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    /**
     * lectureId에 해당하는 특강이 존재하지 않는 경우이므로,
     * 리소스가 없음을 전달 합니다.
     */
    @ExceptionHandler(value = ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> ResourceNotFoundException(Exception e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse("404", e.getMessage()));
    }

    /**
     * 정원이 이미 꽉 찬 상태에서 추가 신청을 하려고 하면,
     * **리소스의 상태(정원)**와 충돌하는 상황임을 전달합니다.
     */
    @ExceptionHandler(value = LectureCapacityExceededException.class)
    public ResponseEntity<ErrorResponse> LectureCapacityExceededException(Exception e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse("409", e.getMessage()));
    }

    @ExceptionHandler(value = ResourceAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> ResourceAlreadyExistsException(Exception e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse("409", e.getMessage()));
    }


}
