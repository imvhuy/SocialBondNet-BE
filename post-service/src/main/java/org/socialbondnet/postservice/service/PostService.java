package org.socialbondnet.postservice.service;

import org.socialbondnet.postservice.model.request.PostRequest;
import org.socialbondnet.postservice.model.response.PostResponse;
import org.springframework.http.ResponseEntity;

public interface PostService {
    ResponseEntity<String> addPost(PostRequest postRequest);
}
