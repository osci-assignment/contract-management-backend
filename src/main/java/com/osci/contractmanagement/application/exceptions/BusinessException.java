package com.osci.contractmanagement.application.exceptions;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private final BusinessExceptionType type;

    public BusinessException(BusinessExceptionType type) {
        super(type.getMessage());
        this.type = type;
    }
}
