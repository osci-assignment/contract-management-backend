package com.osci.contractmanagement.application.dto.request.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class LoginUserRequestDto {
    @Email(message = "이메일 형식이 아닙니다.")
    @NotBlank(message = "이메일을 입력해주세요.")
    @Size(max = 80, message = "이메일의 최대 길이는 80자 입니다")
    private String email;
    @NotBlank(message = "암호를 입력해주세요.")
    @Size(min = 8, max = 20, message = "암호는 8자리 이상, 20자 이하로 입력해주세요.")
    private String password;
}
