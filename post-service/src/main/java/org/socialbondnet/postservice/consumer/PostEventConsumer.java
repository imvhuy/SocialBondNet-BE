package org.socialbondnet.postservice.consumer;
import lombok.RequiredArgsConstructor;
import org.socialbondnet.postservice.model.event.PostCreatedEvent;
import org.socialbondnet.postservice.service.TimelineService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
@Component
@RequiredArgsConstructor
public class PostEventConsumer {
    private final TimelineService timelineService;
    @RabbitListener(queues = "${rabbitmq.json.name}")
    public void handlePostCreatedEvent(PostCreatedEvent event) {
        try {
            timelineService.processNewPost(event);
        } catch (Exception e) {
            System.err.println("Error processing PostCreatedEvent: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
