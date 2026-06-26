package com.osci.contractmanagement.application.dto.request.worker;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CreateWorkerRequestDto {

    @NotBlank(message = "이름을 입력해주세요.")
    private String name;

    @NotBlank(message = "직책을 입력해주세요.")
    private String position;

    @NotBlank(message = "부서를 입력해주세요.")
    private String department;
}
