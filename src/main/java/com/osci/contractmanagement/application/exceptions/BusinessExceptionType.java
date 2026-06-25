package com.osci.contractmanagement.application.exceptions;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum BusinessExceptionType {

    SUCCESS("OSCI0001", HttpStatus.OK, "OK"),
    INVALID_INPUT("OSCI9001", HttpStatus.BAD_REQUEST, "올바르지 않은 입력값입니다."),
    FILE_TOO_LARGE("OSCI9002", HttpStatus.PAYLOAD_TOO_LARGE, "업로드 가능한 파일 크기를 초과했습니다."),
    INTERNAL_SERVER_ERROR("OSCI9999", HttpStatus.INTERNAL_SERVER_ERROR, "알 수 없는 오류가 발생했습니다."),
    USER_NOT_FOUND("OSCI1001", HttpStatus.NOT_FOUND, "유저를 찾을 수 없습니다."),
    PASSWORD_NOT_MATCH("OSCI1003", HttpStatus.BAD_REQUEST, "암호가 일치하지 않습니다."),
    INVALID_TOKEN("OSCI1005", HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    FORBIDDEN("OSCI1004", HttpStatus.FORBIDDEN, "실행할 수 있는 권한이 아닙니다."),
    DUPLICATE_EMAIL("OSCI1002", HttpStatus.CONFLICT, "이미 등록된 이메일입니다."),

    COMPANY_NOT_FOUND("OSCI2001", HttpStatus.NOT_FOUND, "찾을 수 없는 업체입니다."),
    PROJECT_NOT_FOUND("OSCI2002", HttpStatus.NOT_FOUND, "찾을 수 없는 프로젝트입니다."),
    CONTRACT_NOT_FOUND("OSCI2003", HttpStatus.NOT_FOUND, "찾을 수 없는 계약서입니다."),

    WORKER_NOT_FOUND("OSCI3001", HttpStatus.NOT_FOUND, "찾을 수 없는 작업자입니다."),
    WORKER_ALREADY_EXISTS("OSCI3002", HttpStatus.CONFLICT, "이미 작업자로 등록된 유저입니다."),
    USER_NOT_APPROVED("OSCI3003", HttpStatus.BAD_REQUEST, "승인되지 않은 유저는 작업자로 등록할 수 없습니다."),
    ASSIGNMENT_ALREADY_EXISTS("OSCI3004", HttpStatus.CONFLICT, "이미 해당 프로젝트에 배정된 작업자입니다."),
    ASSIGNMENT_NOT_FOUND("OSCI3005", HttpStatus.NOT_FOUND, "찾을 수 없는 배정 정보입니다."),


    EXTRACT_COMPANY_NAME_FAIL("OSCI4001", HttpStatus.CONFLICT, "업체명 추출에 실패했습니다."),
    EXTRACT_CONTRACT_DOCUMENT_FAIL("OSCI4002", HttpStatus.CONFLICT, "계약서 문서 추출 실패"),
    EXTRACT_CONTRACT_INFO_FAIL("OSCI4003", HttpStatus.CONFLICT, "계약서 정보 추출 실패"),
    ;

    private final String code;
    private final HttpStatus status;
    private final String message;
}