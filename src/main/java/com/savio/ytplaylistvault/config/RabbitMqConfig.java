package com.savio.ytplaylistvault.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {
  public static final String SNAPSHOT_CAPTURE_EXCHANGE = "snapshot.capture.exchange";
  public static final String SNAPSHOT_CAPTURE_QUEUE = "snapshot.capture.queue";
  public static final String SNAPSHOT_CAPTURE_ROUTING_KEY = "snapshot.capture.requested";

  public static final String SNAPSHOT_CAPTURE_DEAD_LETTER_EXCHANGE = "snapshot.capture.dlx";
  public static final String SNAPSHOT_CAPTURE_DEAD_LETTER_QUEUE = "snapshot.capture.dlq";
  public static final String SNAPSHOT_CAPTURE_DEAD_LETTER_ROUTING_KEY = "snapshot.capture.failed";

  @Bean
  @ConditionalOnProperty(
      name = "spring.rabbitmq.dynamic",
      havingValue = "true",
      matchIfMissing = true)
  RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
    RabbitAdmin rabbitAdmin = new RabbitAdmin(connectionFactory);
    rabbitAdmin.setAutoStartup(true);
    return rabbitAdmin;
  }

  @Bean
  DirectExchange snapshotCaptureExchange() {
    return new DirectExchange(SNAPSHOT_CAPTURE_EXCHANGE, true, false);
  }

  @Bean
  DirectExchange snapshotCaptureDeadLetterExchange() {
    return new DirectExchange(SNAPSHOT_CAPTURE_DEAD_LETTER_EXCHANGE, true, false);
  }

  @Bean
  Queue snapshotCaptureQueue() {
    return QueueBuilder.durable(SNAPSHOT_CAPTURE_QUEUE)
        .deadLetterExchange(SNAPSHOT_CAPTURE_DEAD_LETTER_EXCHANGE)
        .deadLetterRoutingKey(SNAPSHOT_CAPTURE_DEAD_LETTER_ROUTING_KEY)
        .build();
  }

  @Bean
  Queue snapshotCaptureDeadLetterQueue() {
    return QueueBuilder.durable(SNAPSHOT_CAPTURE_DEAD_LETTER_QUEUE).build();
  }

  @Bean
  Binding snapshotCaptureBinding(
      @Qualifier("snapshotCaptureQueue") Queue snapshotCaptureQueue,
      @Qualifier("snapshotCaptureExchange") DirectExchange snapshotCaptureExchange) {
    return BindingBuilder.bind(snapshotCaptureQueue)
        .to(snapshotCaptureExchange)
        .with(SNAPSHOT_CAPTURE_ROUTING_KEY);
  }

  @Bean
  Binding snapshotCaptureDeadLetterBinding(
      @Qualifier("snapshotCaptureDeadLetterQueue") Queue snapshotCaptureDeadLetterQueue,
      @Qualifier("snapshotCaptureDeadLetterExchange")
          DirectExchange snapshotCaptureDeadLetterExchange) {
    return BindingBuilder.bind(snapshotCaptureDeadLetterQueue)
        .to(snapshotCaptureDeadLetterExchange)
        .with(SNAPSHOT_CAPTURE_DEAD_LETTER_ROUTING_KEY);
  }

  @Bean
  MessageConverter messageConverter() {
    return new JacksonJsonMessageConverter();
  }
}
