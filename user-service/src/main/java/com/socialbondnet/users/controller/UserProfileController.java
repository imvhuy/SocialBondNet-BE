package com.socialbondnet.users.controller;

import com.socialbondnet.users.model.response.ProfileSnapshotResponse;
import com.socialbondnet.users.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class UserProfileController {
    private final IUserService iUserService;
    @GetMapping("/profiles/snapshots")
    public ResponseEntity<Map<String, ProfileSnapshotResponse>> getProfilesInfo(@RequestParam List<String> userIds) {
        return iUserService.getProfileSnapshots(userIds);
    }
}
