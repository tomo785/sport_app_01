package com.sport.api.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 用户信息VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户信息VO")
public class UserVO {

    @Schema(description = "用户ID")
    private Long id;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "头像URL")
    private String avatar;

    @Schema(description = "性别 0未知 1男 2女")
    private Integer gender;

    @Schema(description = "年龄")
    private Integer age;

    @Schema(description = "身高(厘米)")
    private Integer height;

    @Schema(description = "体重(公斤)")
    private BigDecimal weight;
}
