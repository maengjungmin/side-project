package org.side.mjm.domain.user;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.side.mjm.common.request.DynamicSearchRequest;
import org.side.mjm.common.response.structure.ItemResponse;
import org.side.mjm.common.response.structure.ItemsResponse;
import org.side.mjm.domain.user.record.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "[API-002] 회원 정보")
@SecurityRequirement(name = "JWT Token")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping(value = "/user/check", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "ID 중복체크 --> Y 가 나와야 가입 가능한 ID", operationId = "API-002-01")
    public ItemResponse<String> checkNewId(@RequestBody @Valid UserCheckRequest parameter) {
        return userService.checkNewId(parameter);
    }
    @PostMapping(value = "/user/create", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "회원 가입", operationId = "API-002-02")
    public ItemResponse<Long> createUser(@RequestBody @Valid UserCreateRequest parameter) {
        return userService.createUser(parameter);
    }

    @PostMapping(value = "/user/delete", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "회원 탈퇴", operationId = "API-002-03")
    public ItemResponse<Long> deleteUser(@RequestBody @Valid UserCheckRequest parameter) {
        return userService.deleteUser(parameter);
    }

    @PostMapping(value = "/user/info", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "회원 정보 조회", operationId = "API-002-04")
    public ItemResponse<UserSearchResponse> getMyInformation(HttpServletRequest httpServletRequest) {
        return userService.getMyInformation(httpServletRequest);
    }
//
//    @PostMapping(value = "/user/modify", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
//    @Operation(summary = "회원 정보 수정", operationId = "API-002-05")
//    public ItemResponse<Long> modifyUser(@RequestBody @Valid UserModifyRequest parameter, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
//        return userService.modifyUser(parameter, httpServletRequest, httpServletResponse);
//    }
//
//
//    @PostMapping(value = "/user-password/modify", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
//    @Operation(summary = "비밀번호 변경", operationId = "API-002-05")
//    public ItemResponse<UserPasswordModifyResponse> modifyUserPassword(@RequestBody @Valid UserPasswordModifyRequest parameter) {
//        return userService.modifyUserPassword(parameter);
//    }

//    @PostMapping(value = "/user-my-information/search", produces = MediaType.APPLICATION_JSON_VALUE)
//    @Operation(summary = "이메일 인증 시도", operationId = "API-002-07"
//    )
//    public ItemResponse<UserSearchResponse> getMyInformation(HttpServletRequest request) {
//        return userService.getMyInformation(request);
//    }
//    @PostMapping(value = "/user-my-information/search", produces = MediaType.APPLICATION_JSON_VALUE)
//    @Operation(summary = "이메일 인증", operationId = "API-002-07"
//    )
//    public ItemResponse<UserSearchResponse> getMyInformation(HttpServletRequest request) {
//        return userService.getMyInformation(request);
//    }
}
