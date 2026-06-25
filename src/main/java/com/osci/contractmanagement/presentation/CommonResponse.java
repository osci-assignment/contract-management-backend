package com.osci.contractmanagement.presentation;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.osci.contractmanagement.application.exceptions.BusinessExceptionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;


@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CommonResponse<T> {

    private final boolean success;
    private final String code;
    private final String message;
    private final T data;
    private final List<FieldErrorResponse> errors;

    @Getter
    @AllArgsConstructor
    public static class FieldErrorResponse {
        private String field;
        private String message;
    }

    public static <T> ResponseEntity<CommonResponse<T>> ok(T data) {
        return CommonResponse.<T>builder()
                .success(true)
                .message("OK")
                .data(data)
                .build().toResponseEntity(HttpStatus.OK);
    }

    public static <T> ResponseEntity<CommonResponse<T>> ok(T data, String message) {
        return CommonResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build().toResponseEntity(HttpStatus.OK);
    }

    public static <T> ResponseEntity<CommonResponse<T>> error(BusinessExceptionType exception) {
        return CommonResponse.<T>builder()
                .success(false)
                .code(exception.getCode())
                .message(exception.getMessage())
                .build().toResponseEntity(exception.getStatus());
    }
    public static <T> ResponseEntity<CommonResponse<T>> error(List<FieldErrorResponse> errors, BusinessExceptionType exception) {
        return CommonResponse.<T>builder()
                .success(false)
                .code(exception.getCode())
                .message(exception.getMessage())
                .errors(errors)
                .build().toResponseEntity(exception.getStatus());
    }

    private ResponseEntity<CommonResponse<T>> toResponseEntity(HttpStatus status) {
        return ResponseEntity.status(status).body(this);
    }


}