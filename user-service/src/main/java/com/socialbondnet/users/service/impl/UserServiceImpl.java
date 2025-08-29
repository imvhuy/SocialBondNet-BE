package com.socialbondnet.users.service.impl;

import com.socialbondnet.users.entity.UserProfile;
import com.socialbondnet.users.entity.Users;
import com.socialbondnet.users.enums.Visibility;
import com.socialbondnet.users.model.dto.AccountInfoDto;
import com.socialbondnet.users.model.dto.ProfileInfoDto;
import com.socialbondnet.users.model.request.UpdateProfileRequest;
import com.socialbondnet.users.model.response.ProfileResponse;
import com.socialbondnet.users.model.response.PrivateProfileResponse;
import com.socialbondnet.users.model.response.ProfileSnapshotResponse;
import com.socialbondnet.users.model.response.UploadImageResponse;
import com.socialbondnet.users.repository.UserProfileRepository;
import com.socialbondnet.users.repository.UserRepository;
import com.socialbondnet.users.service.IUserService;
import com.socialbondnet.users.service.ObjectStorage;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements IUserService {
    private final UserRepository usersRepository;
    private final UserProfileRepository userProfileRepository;
    private final ObjectStorage storage;

    @Override
    public ProfileResponse  getPublicProfile(String userId, String viewerIdOrNull) {
        Users u = usersRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        // Nếu tài khoản đánh dấu private ở Users hoặc visibility của profile != PUBLIC
        boolean isOwner = viewerIdOrNull != null && viewerIdOrNull.equals(u.getId());
        boolean visible = u.getUserProfile().getVisibility() == Visibility.PUBLIC;

        if (!isOwner && !visible) {
            throw new EntityNotFoundException("Profile is private");
        }
        return toProfileResponse(u);
    }

    @Override
    @Transactional
    public Object getPublicProfileByUsername(String username, String viewerIdOrNull) {
        Users u = usersRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        boolean isOwner = viewerIdOrNull != null && viewerIdOrNull.equals(u.getId());
        UserProfile profile = u.getUserProfile();
        boolean visible = profile != null && profile.getVisibility() == Visibility.PUBLIC;

        if (!isOwner && !visible) {
            // Thay vì ném exception, trả về PrivateProfileResponse
            return PrivateProfileResponse.builder()
                    .userId(u.getId())
                    .visibility(profile != null ? profile.getVisibility() : Visibility.PRIVATE)
                    .username(u.getUsername())
                    .fullName(profile != null ? profile.getFullName() : null)
                    .avatarUrl(profile != null ? profile.getAvatarUrl() : null)
                    .build();
        }
        return toProfileResponse(u);
    }

    @Override
    public ProfileResponse getMyProfile(String userId) {
        Users u = usersRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        return toProfileResponse(u);
    }

    @Override
    public ProfileResponse updateProfile(String userId, UpdateProfileRequest req) {
        Users u = usersRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        UserProfile p = u.getUserProfile();
        if (p == null) {
            p = new UserProfile();
            p.setUser(u);
        }
        if (req.getFullName() != null) p.setFullName(req.getFullName());
        if (req.getGender() != null)    p.setGender(req.getGender());
        if (req.getBio() != null)       p.setBio(req.getBio());
        if (req.getWebsite() != null)   p.setWebsite(req.getWebsite());
        if (req.getLocation() != null)  p.setLocation(req.getLocation());
        if (req.getBirthDate() != null) p.setBirthDate(req.getBirthDate());
        if (req.getVisibility() != null) p.setVisibility(Visibility.valueOf(req.getVisibility()));

        userProfileRepository.save(p);
        u.setUserProfile(p);
        return toProfileResponse(u);
    }

    @Override
    public UploadImageResponse uploadAvatar(String userId, MultipartFile file) {
        Users u = usersRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        UserProfile p = requireProfile(u);

        // Xóa avatar cũ trước khi upload avatar mới
        String oldAvatarUrl = p.getAvatarUrl();
        if (oldAvatarUrl != null && !oldAvatarUrl.isEmpty()) {
            storage.delete(oldAvatarUrl);
        }

        String url = storage.upload("users/%s/avatar".formatted(u.getId()), file);
        p.setAvatarUrl(url);
        userProfileRepository.save(p);
        return new UploadImageResponse(url);
    }

    @Override
    public ResponseEntity<Map<String, ProfileSnapshotResponse>> getProfileSnapshots(List<String> userIds) {
        List<UserProfile> profiles = userProfileRepository.findAllByUserIdIn(userIds);

        if (profiles.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Map<String, ProfileSnapshotResponse> profileMap = profiles.stream()
                .collect(Collectors.toMap(
                        p -> p.getUser().getId(),
                        p -> ProfileSnapshotResponse.builder()
                                .avatarUrl(p.getAvatarUrl())
                                .fullName(p.getFullName())
                                .build()
                ));

        return ResponseEntity.ok(profileMap);
    }

    @Override
    public UploadImageResponse uploadCover(String userId, MultipartFile file) {
        Users u = usersRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        UserProfile p = requireProfile(u);

        // Xóa cover cũ trước khi upload cover mới
        String oldCoverUrl = p.getCoverUrl();
        if (oldCoverUrl != null && !oldCoverUrl.isEmpty()) {
            storage.delete(oldCoverUrl);
        }

        String url = storage.upload("users/%s/cover".formatted(u.getId()), file);
        p.setCoverUrl(url);
        userProfileRepository.save(p);
        return new UploadImageResponse(url);
    }

    private UserProfile requireProfile(Users u) {
        if (u.getUserProfile() == null) {
            UserProfile p = new UserProfile();
            p.setUser(u);
            return p;
        }
        return u.getUserProfile();
    }

    private ProfileResponse toProfileResponse(Users u) {
        AccountInfoDto account = new AccountInfoDto(
                u.getId(),
                u.getEmail(),
                u.getIsActive(),
                u.getCreatedAt(),
                u.getUserProfile() != null ? u.getUserProfile().getVisibility() : Visibility.PUBLIC
        );

        UserProfile p = u.getUserProfile();
        ProfileInfoDto profile;
        if (p == null) {
            profile = new ProfileInfoDto(
                    null, null, null, null, null,
                    null, null, null,
                    Visibility.PUBLIC.name()
            );
        } else {
            profile = new ProfileInfoDto(
                    p.getFullName(),
                    p.getGender(),
                    p.getBio(),
                    p.getWebsite(),
                    p.getLocation(),
                    p.getAvatarUrl(),
                    p.getCoverUrl(),
                    p.getBirthDate(),
                    p.getVisibility().name()
            );
        }

        return new ProfileResponse(account, profile);
    }

}
