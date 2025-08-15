package com.socialbondnet.users.controller;

import com.socialbondnet.users.model.response.ProfileResponse;
import com.socialbondnet.users.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {
    private final IUserService userService;


    @GetMapping("/test/{userId}")
    public ProfileResponse test(@PathVariable String userId) {
        String viewerId = "41cb7867-3d10-4f93-86fb-2bd78e7fa32e";
        return userService.getPublicProfile(userId, viewerId);
    }

    @GetMapping("/test")
    public String test() {
        return "test";
    }
}
