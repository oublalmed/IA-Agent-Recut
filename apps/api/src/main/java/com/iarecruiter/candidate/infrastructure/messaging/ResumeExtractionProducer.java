package com.iarecruiter.candidate.infrastructure.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class ResumeExtractionProducer {

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange}")
    private String exchange;

    @Value("${rabbitmq.routing-keys.resume-extraction}")
    private String routingKey;

    public void sendExtractionRequest(UUID resumeId) {
        log.info("Sending extraction request for resume {}", resumeId);
        rabbitTemplate.convertAndSend(exchange, routingKey,
                new ResumeExtractionMessage(resumeId.toString()));
    }

    public record ResumeExtractionMessage(String resumeId) {}
}
