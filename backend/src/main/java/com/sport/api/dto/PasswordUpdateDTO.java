package com.sport.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 修改密码DTO
 */
@Data
@Schema(description = "修改密码DTO")
public class PasswordUpdateDTO {

    @NotBlank(message = "旧密码不能为空")
    @Schema(description = "旧密码", example = "oldPass123")
    private String oldPassword;

    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 20, message = "新密码长度必须在6-20位之间")
    @Schema(description = "新密码", example = "newPass456")
    private String newPassword;

    @NotBlank(message = "确认密码不能为空")
    @Schema(description = "确认新密码", example = "newPass456")
    private String confirmPassword;
}
