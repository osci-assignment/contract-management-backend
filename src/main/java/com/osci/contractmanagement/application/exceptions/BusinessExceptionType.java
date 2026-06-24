package com.osci.contractmanagement.application.exceptions;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum BusinessExceptionType {

    SUCCESS("OSCI0001", HttpStatus.OK, "OK"),
    INVALID_INPUT("OSCI9001", HttpStatus.BAD_REQUEST, "올바르지 않은 입력값입니다."),
    USER_NOT_FOUND("OSCI1001", HttpStatus.NOT_FOUND, "리소스를 찾을 수 없습니다."),
    PASSWORD_NOT_MATCH("OSCI1003", HttpStatus.BAD_REQUEST, "암호가 일치하지 않습니다."),
    FORBIDDEN("OSCI1004", HttpStatus.FORBIDDEN, "실행할 수 있는 권한이 아닙니다."),
    DUPLICATE_EMAIL("OSCI1001", HttpStatus.CONFLICT, "이미 등록된 이메일입니다.");

    private final String code;
    private final HttpStatus status;
    private final String message;
}
