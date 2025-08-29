package com.socialbondnet.users.controller;

import com.socialbondnet.users.model.request.UpdateProfileRequest;
import com.socialbondnet.users.model.response.ProfileResponse;
import com.socialbondnet.users.model.response.PrivateProfileResponse;
import com.socialbondnet.users.model.response.ProfileSnapshotResponse;
import com.socialbondnet.users.model.response.UploadImageResponse;
import com.socialbondnet.users.service.IUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/profile")
public class UserProfileController {
    private final IUserService iUserService;
    @GetMapping("/snapshots")
    public ResponseEntity<Map<String, ProfileSnapshotResponse>> getProfilesInfo(@RequestParam List<String> userIds) {
        return iUserService.getProfileSnapshots(userIds);
    }

    @GetMapping("/{username}")
    public ResponseEntity<?> getProfile(@PathVariable String username,
                                      @RequestHeader(value = "X-User-Id", required = false) String viewerId,
                                      @RequestHeader(value = "X-Username", required = false) String viewerUsername) {
        // Kiểm tra xem có phải chủ sở hữu profile không
        boolean isOwner = viewerUsername != null && viewerUsername.equals(username);

        if (isOwner) {
            // Nếu là chủ sở hữu, trả về full profile với quyền chỉnh sửa
            ProfileResponse profile = iUserService.getMyProfile(viewerId);
            return ResponseEntity.ok(profile);
        } else {
            // Nếu là người khác, kiểm tra profile có private không
            Object result = iUserService.getPublicProfileByUsername(username, viewerId);
            if (result instanceof PrivateProfileResponse) {
                // Profile private - trả về thông tin hạn chế
                return ResponseEntity.ok(result);
            } else {
                // Profile public - trả về full profile
                return ResponseEntity.ok(result);
            }
        }
    }

    // Cập nhật hồ sơ của chính mình
    @PutMapping()
    public ProfileResponse update(@RequestHeader("X-User-Id") String userId,
                                  @Valid @RequestBody UpdateProfileRequest req) {
        return iUserService.updateProfile(userId, req);
    }

    // Upload avatar
    @PutMapping(path = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ProfileResponse uploadAvatar(@RequestHeader("X-User-Id") String userId,
                                        @RequestPart("file") MultipartFile file) {
        UploadImageResponse uploadResponse = iUserService.uploadAvatar(userId, file);
        return iUserService.getMyProfile(userId);
    }

    // Upload cover
    @PutMapping(path = "/cover", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ProfileResponse uploadCover(@RequestHeader("X-User-Id") String userId,
                                       @RequestPart("file") MultipartFile file) {
        UploadImageResponse uploadResponse = iUserService.uploadCover(userId, file);
        return iUserService.getMyProfile(userId);
    }
}
