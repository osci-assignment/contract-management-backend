package com.sparta.contractmanagement.infrastructure.exception;

import com.sparta.contractmanagement.application.exceptions.BusinessException;
import com.sparta.contractmanagement.application.exceptions.BusinessExceptionType;
import com.sparta.contractmanagement.presentation.CommonResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
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
}

