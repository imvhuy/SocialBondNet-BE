package com.socialbondnet.users.service.impl;

import com.socialbondnet.users.constants.AccountsConstants;
import com.socialbondnet.users.service.ObjectStorage;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class LocalObjectStorageImpl implements ObjectStorage {

    private static final String UPLOAD_DIR = "uploads";
    private static final String BASE_URL = AccountsConstants.BASE_URL + "/files";

    @Override
    public String upload(String keyPrefix, MultipartFile file) {
        try {
            // Tạo thư mục uploads nếu chưa tồn tại
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Tạo thư mục con theo keyPrefix
            Path subPath = uploadPath.resolve(keyPrefix.replace("/", "_"));
            if (!Files.exists(subPath)) {
                Files.createDirectories(subPath);
            }

            // Tạo tên file unique
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String uniqueFilename = UUID.randomUUID().toString() + extension;

            // Lưu file
            Path filePath = subPath.resolve(uniqueFilename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Trả về URL public
            return BASE_URL + "/" + keyPrefix.replace("/", "_") + "/" + uniqueFilename;

        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file: " + e.getMessage(), e);
        }
    }
}
