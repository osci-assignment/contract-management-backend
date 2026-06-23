package com.sparta.contractmanagement.presentation;

import com.sparta.contractmanagement.application.dto.request.CreateUserRequestDto;
import com.sparta.contractmanagement.application.dto.response.UserResponseDto;
import com.sparta.contractmanagement.application.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/user")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<CommonResponse<UserResponseDto>> createUser(@RequestBody @Valid CreateUserRequestDto request) {
        UserResponseDto response = userService.createWorker(request);
        return CommonResponse.ok(response);
    }
}
