package com.sport.api.controller;

import com.sport.api.dto.AdminLoginDTO;
import com.sport.api.vo.LoginVO;
import com.sport.common.util.Result;
import com.sport.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员控制器
 */
@Tag(name = "管理员管理", description = "管理员登录、管理功能")
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;

    @Operation(summary = "管理员登录")
    @PostMapping("/login")
    public Result<LoginVO> adminLogin(@Valid @RequestBody AdminLoginDTO dto) {
        LoginVO loginVO = userService.adminLogin(dto);
        return Result.success(loginVO);
    }
}
