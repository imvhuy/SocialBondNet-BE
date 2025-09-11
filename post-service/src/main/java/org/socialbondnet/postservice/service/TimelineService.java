package org.socialbondnet.postservice.service;

import org.socialbondnet.postservice.model.event.PostCreatedEvent;
import org.socialbondnet.postservice.model.response.TimelineResponse;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface TimelineService {
    void processNewPost(PostCreatedEvent event);
    ResponseEntity<List<TimelineResponse>> getUserTimeline(String userId, int page, int size);
}
