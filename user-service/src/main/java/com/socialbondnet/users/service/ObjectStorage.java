package com.socialbondnet.users.service;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;


public interface ObjectStorage {
    String upload(String keyPrefix, MultipartFile file); // trả URL public
    void delete(String fileUrl); // xóa file theo URL
}
