package com.sport.api.dto;

import lombok.Data;

/**
 * 任务排序DTO
 */
@Data
public class TaskReorderDTO {

    /**
     * 任务ID
     */
    private Long taskId;

    /**
     * 新顺序
     */
    private Integer order;
}
