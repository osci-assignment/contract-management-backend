package com.sparta.contractmanagement.application.exceptions;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum BusinessExceptionType {

    SUCCESS("OSCI0001", HttpStatus.OK, "OK"),
    INVALID_INPUT("OSCI9001", HttpStatus.BAD_REQUEST, "올바르지 않은 입력값입니다."),
    USER_NOT_FOUND("OSCI1001", HttpStatus.NOT_FOUND, "리소스를 찾을 수 없습니다."),
    DUPLICATE_EMAIL("OSCI1001", HttpStatus.CONFLICT, "이미 등록된 이메일입니다.");

    private final String code;
    private final HttpStatus status;
    private final String message;
}
