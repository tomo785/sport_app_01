package com.sport.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 微信运动加密数据解密DTO
 */
@Data
@Schema(description = "微信运动加密数据解密请求")
public class WeRunDecryptDTO {

    @Schema(description = "微信运动加密数据", required = true)
    @NotBlank(message = "encryptedData不能为空")
    private String encryptedData;

    @Schema(description = "加密算法的初始向量", required = true)
    @NotBlank(message = "iv不能为空")
    private String iv;
}
