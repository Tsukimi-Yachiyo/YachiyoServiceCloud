package com.yachiyo.WebSocketService.client;

import com.yachiyo.WebSocketService.dto.Result;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/internal/user/follow/")
public interface UserClient {

    @GetMapping("isFriend")
    Result<Boolean> isFriend(@RequestParam Long currentUserId, @RequestParam Long followeeId);

    @PostMapping("friends")
    Result<List<Long>> friends(@RequestParam Long currentUserId);
}
