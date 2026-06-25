package com.osci.contractmanagement.application.dto.request.worker;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 본인(로그인한 유저)이 작업자 프로필을 등록할 때 쓰는 요청.
 * userId는 인증 정보(@AuthenticationPrincipal)에서 가져오므로 body에 포함하지 않는다.
 */
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
