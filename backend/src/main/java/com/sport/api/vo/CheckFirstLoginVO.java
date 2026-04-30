package com.sport.api.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 首次登录检查返回VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "首次登录检查返回VO")
public class CheckFirstLoginVO {

    @Schema(description = "是否首次登录 true-是 false-否")
    private Boolean isFirstLogin;

    @Schema(description = "用户是否已存在 true-已存在 false-不存在")
    private Boolean userExists;
}
