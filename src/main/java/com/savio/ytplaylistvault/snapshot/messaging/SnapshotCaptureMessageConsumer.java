package com.savio.ytplaylistvault.snapshot.messaging;

import com.savio.ytplaylistvault.config.RabbitMqConfig;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class SnapshotCaptureMessageConsumer {
  private final SnapshotCaptureMessageProcessor snapshotCaptureMessageProcessor;

  public SnapshotCaptureMessageConsumer(
      SnapshotCaptureMessageProcessor snapshotCaptureMessageProcessor) {
    this.snapshotCaptureMessageProcessor = snapshotCaptureMessageProcessor;
  }

  @RabbitListener(queues = RabbitMqConfig.SNAPSHOT_CAPTURE_QUEUE)
  public void consume(SnapshotCaptureRequested request) {
    snapshotCaptureMessageProcessor.process(request);
  }
}
