package com.sport.api.dto;

import lombok.Data;

import java.util.List;

/**
 * 批量更新任务状态DTO
 */
@Data
public class BatchUpdateStatusDTO {

    /**
     * 任务ID列表
     */
    private List<Long> taskIds;

    /**
     * 状态值 (0未开始 1进行中 2已完成)
     */
    private Integer status;
}
