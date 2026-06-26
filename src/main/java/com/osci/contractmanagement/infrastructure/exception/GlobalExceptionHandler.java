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

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<CommonResponse<Void>> handleMaxUploadSizeExceededException(
            MaxUploadSizeExceededException e
    ) {
        log.warn("업로드 파일 크기 초과: {}", e.getMessage());
        return CommonResponse.error(BusinessExceptionType.FILE_TOO_LARGE);
    }

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

