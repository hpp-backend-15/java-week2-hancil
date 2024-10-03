package io.hhplus.clean.application.exception;

import io.hhplus.clean.domain.dto.ErrorResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    /**
     * 정원이 이미 꽉 찬 상태에서 추가 신청을 하려고 하면,
     * 리소스의 상태(정원)와 충돌하는 상황임을 전달합니다.
     */
    @ExceptionHandler(value = LectureCapacityExceededException.class)
    public ResponseEntity<ErrorResponseDTO> LectureCapacityExceededException(Exception e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponseDTO("409", e.getMessage()));
    }

    /**
     * 요청에 해당하는 리소스가 존재하지 않는 경우
     * 리소스가 없음(404)을 전달 합니다.
     */
    @ExceptionHandler(value = ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> ResourceNotFoundException(Exception e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponseDTO("404", e.getMessage()));
    }

    /**
     * 요청에 해당하는 리소스가 이미 존재 하는 경우
     * 리소스의 상태와 충돌하는 상황임을 전달합니다.
     */
    @ExceptionHandler(value = ResourceAlreadyExistsException.class)
    public ResponseEntity<ErrorResponseDTO> ResourceAlreadyExistsException(Exception e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponseDTO("409", e.getMessage()));
    }


}
