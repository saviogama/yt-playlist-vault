package com.savio.ytplaylistvault.snapshot.messaging;

import com.savio.ytplaylistvault.config.RabbitMqConfig;
import java.util.UUID;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class SnapshotCaptureMessagePublisher {
  private final RabbitTemplate rabbitTemplate;

  public SnapshotCaptureMessagePublisher(RabbitTemplate rabbitTemplate) {
    this.rabbitTemplate = rabbitTemplate;
  }

  public void publish(UUID monitoredPlaylistId) {
    SnapshotCaptureRequested request = new SnapshotCaptureRequested(monitoredPlaylistId);

    rabbitTemplate.convertAndSend(
        RabbitMqConfig.SNAPSHOT_CAPTURE_EXCHANGE,
        RabbitMqConfig.SNAPSHOT_CAPTURE_ROUTING_KEY,
        request);
  }
}
