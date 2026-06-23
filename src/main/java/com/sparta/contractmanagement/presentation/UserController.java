package com.sparta.contractmanagement.presentation;

import com.sparta.contractmanagement.application.dto.request.auth.LoginUserDto;
import com.sparta.contractmanagement.application.dto.request.user.CreateAdminUserRequestDto;
import com.sparta.contractmanagement.application.dto.request.user.CreateUserRequestDto;
import com.sparta.contractmanagement.application.dto.response.auth.TokenResponseDto;
import com.sparta.contractmanagement.application.dto.response.user.UserResponseDto;
import com.sparta.contractmanagement.application.service.user.UserCommandService;
import com.sparta.contractmanagement.application.service.user.UserServiceFacade;
import jakarta.validation.Valid;
import org.antlr.v4.runtime.Token;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserServiceFacade userServiceFacade;

    public UserController(UserServiceFacade userServiceFacade) {
        this.userServiceFacade = userServiceFacade;
    }


    @PostMapping
    public ResponseEntity<CommonResponse<UserResponseDto>> createUser(@RequestBody @Valid CreateUserRequestDto request) {
        UserResponseDto response = userServiceFacade.createWorker(request);
        return CommonResponse.ok(response);
    }

    @PostMapping("/admin")
    public ResponseEntity<CommonResponse<UserResponseDto>> createAdminUser(@RequestBody @Valid CreateAdminUserRequestDto request) {
        UserResponseDto response = userServiceFacade.createAdmin(request);
        return CommonResponse.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<CommonResponse<TokenResponseDto>> login(@RequestBody @Valid LoginUserDto request) {
        TokenResponseDto response = userServiceFacade.login(request);
        return CommonResponse.ok(response);
    }
}
