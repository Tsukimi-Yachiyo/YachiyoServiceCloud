package com.yachiyo.WebSocketService.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Data @NoArgsConstructor
@AllArgsConstructor @Slf4j
public class Result<T>{
    private String code;
    private String message;
    private T data;
    private String detail = null;

    public static <T> Result<T> success(T data){
        Result<T> result = new Result<>();
        result.setCode("200");
        result.setMessage("success");
        result.setData(data);
        return result;
    }
}
