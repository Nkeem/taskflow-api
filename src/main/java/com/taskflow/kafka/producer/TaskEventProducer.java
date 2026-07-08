package com.taskflow.kafka.producer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.taskflow.kafka.event.TaskStatusChangedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskEventProducer {

    private final KafkaTemplate<String, TaskStatusChangedEvent> kafkaTemplate;

    @Value("${app.kafka.topics.task-events}")
    private String taskEventsTopic;

    public void sendTaskStatusChangedEvent(TaskStatusChangedEvent event) {
        kafkaTemplate.send(taskEventsTopic, event.taskId().toString(), event);
        log.info(
                "Sent task status changed event: taskId={}, oldStatus={}, newStatus={}",
                event.taskId(),
                event.oldStatus(),
                event.newStatus()
        );
    }
}
