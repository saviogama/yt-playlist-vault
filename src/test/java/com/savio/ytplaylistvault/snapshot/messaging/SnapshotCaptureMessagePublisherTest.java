package com.savio.ytplaylistvault.snapshot.messaging;

import static org.mockito.Mockito.verify;

import com.savio.ytplaylistvault.config.RabbitMqConfig;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

class SnapshotCaptureMessagePublisherTest {

  private final RabbitTemplate rabbitTemplate = Mockito.mock(RabbitTemplate.class);
  private final SnapshotCaptureMessagePublisher publisher =
      new SnapshotCaptureMessagePublisher(rabbitTemplate);

  @Test
  void publishesSnapshotCaptureRequestToConfiguredRoute() {
    UUID monitoredPlaylistId = UUID.randomUUID();

    publisher.publish(monitoredPlaylistId);

    verify(rabbitTemplate)
        .convertAndSend(
            RabbitMqConfig.SNAPSHOT_CAPTURE_EXCHANGE,
            RabbitMqConfig.SNAPSHOT_CAPTURE_ROUTING_KEY,
            new SnapshotCaptureRequested(monitoredPlaylistId));
  }
}
