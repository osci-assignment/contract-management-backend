package com.osci.contractmanagement.infrastructure.exception;

import com.osci.contractmanagement.application.exceptions.BusinessException;
import com.osci.contractmanagement.application.exceptions.BusinessExceptionType;
import com.osci.contractmanagement.presentation.CommonResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.List;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CommonResponse<Void>> handleValidationException(
            MethodArgumentNotValidException e
    ) {

        List<CommonResponse.FieldErrorResponse> errors =
                e.getBindingResult()
                        .getFieldErrors()
                        .stream()
                        .map(error ->
                                new CommonResponse.FieldErrorResponse(
                                        error.getField(),
                                        error.getDefaultMessage()
                                )
                        )
                        .toList();

        return CommonResponse.error(errors, BusinessExceptionType.INVALID_INPUT);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<CommonResponse<Void>> handleBusinessException(
            BusinessException e
    ) {
        return CommonResponse.error(e.getType());
    }

    /**
     * Spring이 던지는 시스템성 예외(업로드 용량 초과 등)도 BusinessException과 동일한
     * 응답 형태(success=false, code, message)로 변환해 클라이언트가 한 가지 형식만
     * 처리하면 되게 한다.
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<CommonResponse<Void>> handleMaxUploadSizeExceededException(
            MaxUploadSizeExceededException e
    ) {
        log.warn("업로드 파일 크기 초과: {}", e.getMessage());
        return CommonResponse.error(BusinessExceptionType.FILE_TOO_LARGE);
    }

    /**
     * 위에서 처리하지 못한, 예상하지 못한 모든 예외의 최종 안전망.
     * 원인을 클라이언트에 노출하지 않고 일반화된 메시지만 내려주며,
     * 다른 BusinessException들과 달리 실제 HTTP 상태코드도 500으로 내려서
     * 모니터링/알람에서 "예상 못한 오류"로 명확히 구분되게 한다.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<CommonResponse<Void>> handleUnexpectedException(Exception e) {
        log.error("처리되지 않은 예외 발생", e);

        CommonResponse<Void> body = CommonResponse.<Void>builder()
                .success(false)
                .code(BusinessExceptionType.INTERNAL_SERVER_ERROR.getCode())
                .message(BusinessExceptionType.INTERNAL_SERVER_ERROR.getMessage())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}

