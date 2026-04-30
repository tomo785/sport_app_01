package com.sport.api.controller;

import com.sport.api.dto.LoginDTO;
import com.sport.api.dto.PasswordLoginDTO;
import com.sport.api.dto.RegisterDTO;
import com.sport.api.dto.SmsCodeDTO;
import com.sport.api.dto.UserUpdateDTO;
import com.sport.api.vo.CheckFirstLoginVO;
import com.sport.api.vo.LoginVO;
import com.sport.api.vo.UserVO;
import com.sport.common.constant.ResultCode;
import com.sport.common.util.JwtUtil;
import com.sport.common.util.Result;
import com.sport.model.entity.User;
import com.sport.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器
 */
@Tag(name = "用户管理", description = "用户注册、登录、信息管理")
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    @Operation(summary = "发送短信验证码")
    @PostMapping("/sms-code")
    public Result<Void> sendSmsCode(@Valid @RequestBody SmsCodeDTO dto) {
        userService.sendSmsCode(dto);
        return Result.success();
    }

    @Operation(summary = "手机号验证码登录")
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        LoginVO loginVO = userService.login(dto);
        return Result.success(loginVO);
    }

    @Operation(summary = "账号密码登录")
    @PostMapping("/login/password")
    public Result<LoginVO> loginByPassword(@Valid @RequestBody PasswordLoginDTO dto) {
        LoginVO loginVO = userService.loginByPassword(dto);
        return Result.success(loginVO);
    }

    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public Result<LoginVO> register(@Valid @RequestBody RegisterDTO dto) {
        LoginVO loginVO = userService.register(dto);
        return Result.success(loginVO);
    }

    @Operation(summary = "检查是否首次登录")
    @GetMapping("/check-first-login")
    public Result<CheckFirstLoginVO> checkFirstLogin(
            @Parameter(description = "手机号") @RequestParam String phone) {
        CheckFirstLoginVO vo = userService.checkFirstLogin(phone);
        return Result.success(vo);
    }

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/info")
    public Result<UserVO> getUserInfo(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            return Result.fail(ResultCode.UNAUTHORIZED);
        }
        token = token.substring(7);

        Long userId = jwtUtil.getUserIdFromToken(token);
        User user = userService.getUserById(userId);
        UserVO userVO = userService.toVO(user);
        return Result.success(userVO);
    }

    @Operation(summary = "更新当前用户信息")
    @PutMapping("/info")
    public Result<UserVO> updateUserInfo(
            @Valid @RequestBody UserUpdateDTO dto,
            HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            return Result.fail(ResultCode.UNAUTHORIZED);
        }
        token = token.substring(7);

        Long userId = jwtUtil.getUserIdFromToken(token);
        User user = userService.getUserById(userId);
        if (user == null) {
            return Result.fail(ResultCode.USER_NOT_EXIST);
        }

        BeanUtils.copyProperties(dto, user);
        userService.updateUser(user);

        UserVO userVO = userService.toVO(user);
        return Result.success(userVO);
    }

    @Operation(summary = "修改密码")
    @PutMapping("/password")
    public Result<Void> updatePassword(
            @Valid @RequestBody com.sport.api.dto.PasswordUpdateDTO dto,
            HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            return Result.fail(ResultCode.UNAUTHORIZED);
        }
        token = token.substring(7);

        Long userId = jwtUtil.getUserIdFromToken(token);

        // 校验两次密码是否一致
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            return Result.fail(ResultCode.PASSWORD_NOT_MATCH);
        }

        userService.updatePassword(userId, dto.getOldPassword(), dto.getNewPassword());
        return Result.success();
    }

    @Operation(summary = "上传头像")
    @PostMapping("/avatar")
    public Result<String> uploadAvatar(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            return Result.fail(ResultCode.UNAUTHORIZED);
        }
        token = token.substring(7);

        Long userId = jwtUtil.getUserIdFromToken(token);
        User user = userService.getUserById(userId);
        if (user == null) {
            return Result.fail(ResultCode.USER_NOT_EXIST);
        }

        // TODO: 实现头像上传逻辑，暂时返回空字符串
        return Result.success("");
    }
}
