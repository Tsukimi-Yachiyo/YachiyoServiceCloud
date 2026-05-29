package com.yachiyo.WebSocketService.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@TableName("player_position")
@Data
public class PlayerPosition{

    private Long id;

    private Float x;

    private Float y;
}
