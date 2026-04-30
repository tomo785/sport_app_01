package com.sport.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 管理员登录DTO
 */
@Data
@Schema(description = "管理员登录请求")
public class AdminLoginDTO {

    @NotBlank(message = "账号不能为空")
    @Schema(description = "管理员账号(用户名)", requiredMode = Schema.RequiredMode.REQUIRED)
    private String username;

    @NotBlank(message = "密码不能为空")
    @Schema(description = "管理员密码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;
}
