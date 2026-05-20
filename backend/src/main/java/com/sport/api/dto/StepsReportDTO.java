package com.sport.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 步数上报DTO
 */
@Data
@Schema(description = "步数上报请求")
public class StepsReportDTO {

    @Schema(description = "步数", required = true)
    @NotNull(message = "步数不能为空")
    @Min(value = 0, message = "步数不能为负数")
    private Integer steps;

    @Schema(description = "卡路里(千卡)")
    private Integer calories;

    @Schema(description = "距离(米)")
    private Integer distance;

    @Schema(description = "时长(秒)")
    private Integer duration;

    @Schema(description = "数据来源：health/pedometer/werun/backend")
    private String source;
}
