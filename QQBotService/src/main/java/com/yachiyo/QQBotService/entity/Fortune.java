package com.yachiyo.QQBotService.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;

@Data
@TableName("fortunes")
public class Fortune {
    private Long qq;
    private LocalDate date;
    private Integer result;
}
