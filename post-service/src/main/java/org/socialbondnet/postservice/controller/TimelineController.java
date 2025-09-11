package org.socialbondnet.postservice.controller;
import lombok.RequiredArgsConstructor;
import org.socialbondnet.postservice.model.response.TimelineResponse;
import org.socialbondnet.postservice.service.TimelineService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/timeline")
@RequiredArgsConstructor
public class TimelineController {
    private final TimelineService timelineService;
    @GetMapping("/{userId}")
    public ResponseEntity<List<TimelineResponse>> getUserTimeline(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return timelineService.getUserTimeline(userId, page, size);
    }
}
