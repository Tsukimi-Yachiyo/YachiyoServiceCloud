package com.yachiyo.AdminService.controller;

import com.yachiyo.AdminService.dto.PostingQueryRequest;
import com.yachiyo.AdminService.dto.ReviewRequest;
import com.yachiyo.AdminService.dto.PostingResponse;
import com.yachiyo.AdminService.dto.UserResponse;
import com.yachiyo.AdminService.result.Result;
import com.yachiyo.AdminService.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/yachiyo/168/mini/admin")
@RequiredArgsConstructor
@Validated
public class AdminController {

    private final AdminService adminService;

    @PostMapping("/run-command")
    public Result<String> runCommand(@RequestParam("command") String command) {
        return adminService.RunCommand(command);
    }

    @PostMapping("/login")
    public Result<String> login(@RequestParam("username") String username, @RequestParam("password") String password) {
        UserResponse user = new UserResponse();
        user.setName(username);
        user.setPassword(password);
        return adminService.Login(user);
    }

    @PostMapping("/review")
    public Result<Boolean> reviewPosting(@RequestBody @Valid ReviewRequest request) {
        return adminService.reviewPosting(request);
    }

    @PostMapping("/query-postings")
    public Result<List<PostingResponse>> queryPostings(@RequestBody @Valid PostingQueryRequest request) {
        return adminService.queryPostings(request);
    }
}
