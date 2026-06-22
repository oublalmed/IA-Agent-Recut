package com.iarecruiter.candidate.infrastructure.messaging;

import com.iarecruiter.candidate.application.usecase.ExtractResumeUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import com.rabbitmq.client.Channel;

import java.io.IOException;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class ResumeExtractionConsumer {

    private final ExtractResumeUseCase extractResumeUseCase;

    @RabbitListener(queues = "${rabbitmq.queues.resume-extraction}", containerFactory = "rabbitListenerContainerFactory")
    public void handleExtractionRequest(ResumeExtractionProducer.ResumeExtractionMessage message,
                                         Message amqpMessage, Channel channel) throws IOException {
        long deliveryTag = amqpMessage.getMessageProperties().getDeliveryTag();
        try {
            UUID resumeId = UUID.fromString(message.resumeId());
            log.info("Processing extraction for resume {}", resumeId);
            extractResumeUseCase.execute(resumeId);
            channel.basicAck(deliveryTag, false);
            log.info("Extraction complete for resume {}", resumeId);
        } catch (Exception e) {
            log.error("Extraction failed for resume {}: {}", message.resumeId(), e.getMessage());
            // Nack with requeue=false to send to DLQ after max retries
            channel.basicNack(deliveryTag, false, false);
        }
    }
}
