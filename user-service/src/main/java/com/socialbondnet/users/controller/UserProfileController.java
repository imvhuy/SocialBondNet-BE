package com.socialbondnet.users.controller;

import com.socialbondnet.users.model.request.UpdateProfileRequest;
import com.socialbondnet.users.model.response.ProfileResponse;
import com.socialbondnet.users.model.response.PrivateProfileResponse;
import com.socialbondnet.users.model.response.UploadImageResponse;
import com.socialbondnet.users.service.IUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class UserProfileController {
    private final IUserService userService;


    @GetMapping("/profile/{username}")
    public ResponseEntity<?> getProfile(@PathVariable String username,
                                      @RequestHeader(value = "X-User-Id", required = false) String viewerId,
                                      @RequestHeader(value = "X-Username", required = false) String viewerUsername) {
        // Kiểm tra xem có phải chủ sở hữu profile không
        boolean isOwner = viewerUsername != null && viewerUsername.equals(username);

        if (isOwner) {
            // Nếu là chủ sở hữu, trả về full profile với quyền chỉnh sửa
            ProfileResponse profile = userService.getMyProfile(viewerId);
            return ResponseEntity.ok(profile);
        } else {
            // Nếu là người khác, kiểm tra profile có private không
            Object result = userService.getPublicProfileByUsername(username, viewerId);
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
    @PutMapping("/profile")
    public ProfileResponse update(@RequestHeader("X-User-Id") String userId,
                                  @Valid @RequestBody UpdateProfileRequest req) {
        return userService.updateProfile(userId, req);
    }

    // Upload avatar
    @PutMapping(path = "/profile/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ProfileResponse uploadAvatar(@RequestHeader("X-User-Id") String userId,
                                        @RequestPart("file") MultipartFile file) {
        UploadImageResponse uploadResponse = userService.uploadAvatar(userId, file);
        return userService.getMyProfile(userId);
    }

    // Upload cover
    @PutMapping(path = "/profile/cover", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ProfileResponse uploadCover(@RequestHeader("X-User-Id") String userId,
                                       @RequestPart("file") MultipartFile file) {
        UploadImageResponse uploadResponse = userService.uploadCover(userId, file);
        return userService.getMyProfile(userId);
    }
}
