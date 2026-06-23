package com.sparta.contractmanagement.application.exceptions;

import com.sparta.contractmanagement.presentation.CommonResponse;
import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private final BusinessExceptionType type;

    public BusinessException(BusinessExceptionType type) {
        super(type.getMessage());
        this.type = type;
    }
}
