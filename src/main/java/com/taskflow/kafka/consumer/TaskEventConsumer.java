package com.taskflow.kafka.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.taskflow.kafka.event.TaskStatusChangedEvent;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class TaskEventConsumer {

    @KafkaListener(
            topics = "${app.kafka.topics.task-events}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void listen(TaskStatusChangedEvent event) {
        log.info(
                "Received task status changed event: taskId={}, projectId={}, oldStatus={}, newStatus={}, createdAt={}",
                event.taskId(),
                event.projectId(),
                event.oldStatus(),
                event.newStatus(),
                event.createdAt()
        );
    }
}
