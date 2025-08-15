package com.socialbondnet.users.controller;

import com.socialbondnet.users.model.response.ProfileResponse;
import com.socialbondnet.users.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class UserController {
    private final IUserService userService;


    @GetMapping("/test/{userId}")
    public ProfileResponse test(@PathVariable String userId) {
        String viewerId = "f9253b3a-da9c-4b5c-88d3-1b3b79dd2b40";
        return userService.getPublicProfile(userId, viewerId);
    }
}
