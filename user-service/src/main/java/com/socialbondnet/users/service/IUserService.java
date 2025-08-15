package com.socialbondnet.users.service;

import com.socialbondnet.users.entity.UserProfile;
import com.socialbondnet.users.entity.Users;
import com.socialbondnet.users.model.request.UpdateProfileRequest;
import com.socialbondnet.users.model.response.ProfileResponse;
import com.socialbondnet.users.model.response.UploadImageResponse;
import jakarta.transaction.Transactional;
import org.springframework.web.multipart.MultipartFile;

public interface IUserService {
    ProfileResponse getPublicProfile(String userId, String viewerIdOrNull);
    ProfileResponse getMyProfile(String userId);
    @Transactional
    ProfileResponse updateProfile(String userId, UpdateProfileRequest updateProfileRequest);
    @Transactional
    UploadImageResponse uploadAvatar(String userId, MultipartFile file);



}
