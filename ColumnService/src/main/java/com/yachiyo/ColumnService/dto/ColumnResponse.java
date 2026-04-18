package com.yachiyo.ColumnService.dto;

import lombok.Data;

@Data
public class ColumnResponse {
    private Long id;

    private String name;

    private String description;

    private EssayType type;

    private Long writer;

    private String essayUrl;

    private Data createTime;

}
